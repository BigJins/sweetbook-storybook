package com.sweetbook.web;

import com.sweetbook.service.StoryService;
import com.sweetbook.web.dto.StoryDto;
import com.sweetbook.web.dto.StorySummaryDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
