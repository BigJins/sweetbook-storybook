package com.sweetbook.web.dto;

import com.sweetbook.domain.story.StoryStatus;

import java.time.Instant;

public record StorySummaryDto(
    String id,
    String title,
    String childName,
    StoryStatus status,
    String coverUrl,
    Instant createdAt,
    String errorMessage
) {}
