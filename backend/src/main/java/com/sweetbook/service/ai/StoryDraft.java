package com.sweetbook.service.ai;

import java.util.List;

public record StoryDraft(String title, List<PageDraft> pages) {
    public record PageDraft(int pageNumber, String bodyText, String illustrationPrompt) {}
}
