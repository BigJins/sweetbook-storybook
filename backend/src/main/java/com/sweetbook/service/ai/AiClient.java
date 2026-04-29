package com.sweetbook.service.ai;

import com.sweetbook.domain.story.PageLayout;

public interface AiClient {

    StyleDescriptor analyzeDrawing(byte[] drawingBytes, String contentType);

    StoryDraft generateStory(String childName, String imaginationPrompt, StyleDescriptor style);

    byte[] generateIllustration(String prompt, StyleDescriptor style, PageLayout layout, int pageNumber);
}
