package com.sweetbook.web.dto;

import jakarta.validation.constraints.Size;

public record PageBodyUpdateRequest(
    @Size(max = 500, message = "본문은 500자 이하로 적어주세요")
    String bodyText
) {}
