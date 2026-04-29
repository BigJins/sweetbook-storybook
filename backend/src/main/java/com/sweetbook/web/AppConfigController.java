package com.sweetbook.web;

import com.sweetbook.config.MockDemoStoryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AppConfigController {

    private final boolean mockMode;
    private final MockDemoStoryConfig demoStory;

    public AppConfigController(@Value("${app.ai.mock-mode:true}") boolean mockMode,
                               MockDemoStoryConfig demoStory) {
        this.mockMode = mockMode;
        this.demoStory = demoStory;
    }

    @GetMapping("/api/app-config")
    public AppConfigDto config() {
        DemoStoryDto demo = mockMode
            ? new DemoStoryDto(
                demoStory.childName(),
                demoStory.imaginationPrompt(),
                "/api/app-config/demo-drawing.png"
            )
            : null;
        return new AppConfigDto(mockMode, demo);
    }

    @GetMapping(value = "/api/app-config/demo-drawing.png", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] demoDrawing() throws IOException {
        return new ClassPathResource(demoStory.drawingResource()).getInputStream().readAllBytes();
    }

    public record AppConfigDto(boolean mockMode, DemoStoryDto demoStory) {}

    public record DemoStoryDto(String childName, String imaginationPrompt, String drawingUrl) {}
}
