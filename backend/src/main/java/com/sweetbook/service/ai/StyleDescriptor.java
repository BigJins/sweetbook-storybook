package com.sweetbook.service.ai;

import java.util.List;

public record StyleDescriptor(List<String> keywords) {
    public String asPromptPrefix() {
        return String.join(", ", keywords);
    }
}
