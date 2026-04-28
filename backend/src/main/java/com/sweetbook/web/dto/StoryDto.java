package com.sweetbook.web.dto;

import com.sweetbook.domain.story.StoryStatus;

import java.time.Instant;
import java.util.List;

public record StoryDto(
    String id,
    String title,
    String childName,
    StoryStatus status,
    String errorMessage,
    String drawingUrl,
    String styleDescriptor,
    String imaginationPrompt,
    List<PageDto> pages,
    Instant createdAt
) {}
