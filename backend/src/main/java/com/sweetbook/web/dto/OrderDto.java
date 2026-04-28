package com.sweetbook.web.dto;

import com.sweetbook.domain.order.OrderStatus;

import java.time.Instant;

public record OrderDto(
    String id,
    OrderStorySummary story,
    OrderStatus status,
    String recipientName,
    String addressMemo,
    OrderItemDto item,
    Instant createdAt
) {
    public record OrderStorySummary(String id, String title, String coverUrl) {}
}
