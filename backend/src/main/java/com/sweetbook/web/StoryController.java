package com.sweetbook.web;

import com.sweetbook.domain.story.Story;
import com.sweetbook.service.StoryService;
import com.sweetbook.web.dto.StoryCreateRequest;
import com.sweetbook.web.dto.StoryDto;
import com.sweetbook.web.dto.StorySummaryDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @GetMapping
    public List<StorySummaryDto> list() {
        return storyService.list();
    }

    @GetMapping("/{id}")
    public StoryDto detail(@PathVariable String id) {
        return storyService.getById(id);
    }

    @PostMapping
    public Map<String, String> create(
        @Valid @ModelAttribute StoryCreateRequest req,
        @RequestParam("drawing") MultipartFile drawing
    ) throws IOException {
        Story s = storyService.create(req, drawing);
        storyService.kickOffAsyncGeneration(s.getId());
        return Map.of("id", s.getId(), "status", s.getStatus().name());
    }
}
