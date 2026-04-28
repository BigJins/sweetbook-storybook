package com.sweetbook.domain.story;

import java.util.Map;
import java.util.Set;

public enum StoryStatus {
    DRAFT,
    ANALYZING_DRAWING,
    GENERATING_STORY,
    GENERATING_IMAGES,
    COMPLETED,
    FAILED;

    private static final Map<StoryStatus, Set<StoryStatus>> ALLOWED = Map.of(
            DRAFT, Set.of(ANALYZING_DRAWING, FAILED),
            ANALYZING_DRAWING, Set.of(GENERATING_STORY, FAILED),
            GENERATING_STORY, Set.of(GENERATING_IMAGES, FAILED),
            GENERATING_IMAGES, Set.of(COMPLETED, FAILED),
            COMPLETED, Set.of(),
            FAILED, Set.of(ANALYZING_DRAWING)
    );

    public boolean canTransitionTo(StoryStatus target) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
