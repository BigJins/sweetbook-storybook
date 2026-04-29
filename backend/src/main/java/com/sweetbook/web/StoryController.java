package com.sweetbook.web;

import com.sweetbook.domain.story.Story;
import com.sweetbook.service.StoryGenerationService;
import com.sweetbook.service.StoryService;
import com.sweetbook.web.dto.PageBodyUpdateRequest;
import com.sweetbook.web.dto.StoryCreateRequest;
import com.sweetbook.web.dto.StoryDto;
import com.sweetbook.web.dto.StorySummaryDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final StoryGenerationService generationService;

    public StoryController(StoryService storyService, StoryGenerationService generationService) {
        this.storyService = storyService;
        this.generationService = generationService;
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
        @RequestParam(value = "drawing", required = false) MultipartFile drawing
    ) throws IOException {
        Story s = storyService.create(req, drawing);
        storyService.kickOffAsyncGeneration(s.getId());
        return Map.of("id", s.getId(), "status", s.getStatus().name());
    }

    @PatchMapping("/{id}/pages/{n}")
    public Map<String, Boolean> updatePageBody(
        @PathVariable String id,
        @PathVariable int n,
        @Valid @RequestBody PageBodyUpdateRequest req
    ) {
        storyService.updatePageBody(id, n, req.bodyText());
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/pages/{n}/regenerate")
    public Map<String, Boolean> regenerate(@PathVariable String id, @PathVariable int n) {
        generationService.regeneratePage(id, n);
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/retry")
    public Map<String, Boolean> retry(@PathVariable String id) {
        generationService.retry(id);
        return Map.of("ok", true);
    }
}
