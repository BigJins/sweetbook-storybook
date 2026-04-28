package com.sweetbook.service.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record StyleDescriptor(
    List<String> keywords,
    String subject,
    String subjectType,
    String mood,
    List<String> sceneCues
) {
    public StyleDescriptor {
        if (keywords == null) keywords = List.of();
        if (sceneCues == null) sceneCues = List.of();
    }

    public static StyleDescriptor empty() {
        return new StyleDescriptor(List.of(), null, null, null, List.of());
    }

    public String asPromptPrefix() {
        return String.join(", ", keywords);
    }

    public String subjectOrFallback() {
        return (subject == null || subject.isBlank()) ? "그림 속 주인공" : subject;
    }

    public String moodOrFallback() {
        return (mood == null || mood.isBlank()) ? "따뜻한" : mood;
    }
}
