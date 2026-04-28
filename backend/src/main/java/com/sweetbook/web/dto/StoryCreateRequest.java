package com.sweetbook.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoryCreateRequest(
    @NotBlank(message = "아이 이름은 1~20자로 적어주세요")
    @Size(min = 1, max = 20, message = "아이 이름은 1~20자로 적어주세요")
    String childName,

    @NotBlank(message = "상상은 10자 이상 500자 이하로 적어주세요")
    @Size(min = 10, max = 500, message = "상상은 10자 이상 500자 이하로 적어주세요")
    String imaginationPrompt
) {}
