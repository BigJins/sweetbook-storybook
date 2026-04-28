package com.sweetbook.web.dto;

import com.sweetbook.domain.order.BookSize;
import com.sweetbook.domain.order.CoverType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderCreateRequest(
    @NotBlank String storyId,
    @NotNull BookSize bookSize,
    @NotNull CoverType coverType,
    @Min(1) @Max(10) int copies,
    @NotBlank @Size(min = 1, max = 30, message = "받는 분 이름은 1~30자로 적어주세요") String recipientName,
    @Size(max = 500) String addressMemo
) {}
