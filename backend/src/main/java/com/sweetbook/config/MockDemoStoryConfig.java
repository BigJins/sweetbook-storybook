package com.sweetbook.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MockDemoStoryConfig {

    private static final String CHILD_NAME = "서아";
    private static final String IMAGINATION_PROMPT = "공주님과 왕자님이 만나 행복하게 사는 이야기, 그림은 되도록 아이의 그림을 잘 살릴 수 있도록, 너무 고퀄보다 아이그림이 살아 있다는 느낌으로";
    private static final String DRAWING_RESOURCE = "mock/demo/drawing.png";

    public String childName() {
        return CHILD_NAME;
    }

    public String imaginationPrompt() {
        return IMAGINATION_PROMPT;
    }

    public String drawingResource() {
        return DRAWING_RESOURCE;
    }

    public byte[] drawingBytes() throws IOException {
        return new ClassPathResource(DRAWING_RESOURCE).getInputStream().readAllBytes();
    }
}
