package com.sweetbook.web.dto;

import com.sweetbook.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
    @NotNull(message = "status는 필수입니다") OrderStatus status
) {}
