package com.sweetbook.web.dto;

import com.sweetbook.domain.story.PageLayout;

public record PageDto(
    int pageNumber,
    PageLayout layout,
    String bodyText,
    String illustrationPrompt,
    String illustrationUrl
) {}
