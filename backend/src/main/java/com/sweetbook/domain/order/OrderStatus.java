package com.sweetbook.domain.order;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    COMPLETED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            PENDING, Set.of(PROCESSING),
            PROCESSING, Set.of(COMPLETED),
            COMPLETED, Set.of()
    );

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(target);
    }
}
