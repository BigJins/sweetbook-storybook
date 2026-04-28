package com.sweetbook.service.ai;

public interface AiClient {

    StyleDescriptor analyzeDrawing(byte[] drawingBytes, String contentType);

    StoryDraft generateStory(String childName, String imaginationPrompt, StyleDescriptor style);

    byte[] generateIllustration(String prompt, StyleDescriptor style);
}
