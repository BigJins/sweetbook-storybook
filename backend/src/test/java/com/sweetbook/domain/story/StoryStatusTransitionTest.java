package com.sweetbook.domain.story;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoryStatusTransitionTest {

    @Test
    void normalForwardPathIsAllowed() {
        assertTrue(StoryStatus.DRAFT.canTransitionTo(StoryStatus.ANALYZING_DRAWING));
        assertTrue(StoryStatus.ANALYZING_DRAWING.canTransitionTo(StoryStatus.GENERATING_STORY));
        assertTrue(StoryStatus.GENERATING_STORY.canTransitionTo(StoryStatus.GENERATING_IMAGES));
        assertTrue(StoryStatus.GENERATING_IMAGES.canTransitionTo(StoryStatus.COMPLETED));
    }

    @Test
    void retryIsAllowedOnlyFromFailed() {
        assertTrue(StoryStatus.FAILED.canTransitionTo(StoryStatus.ANALYZING_DRAWING));
        assertFalse(StoryStatus.COMPLETED.canTransitionTo(StoryStatus.ANALYZING_DRAWING));
        assertFalse(StoryStatus.DRAFT.canTransitionTo(StoryStatus.GENERATING_STORY));
    }

    @Test
    void terminalStatesAreReported() {
        assertTrue(StoryStatus.COMPLETED.isTerminal());
        assertTrue(StoryStatus.FAILED.isTerminal());
        assertFalse(StoryStatus.GENERATING_STORY.isTerminal());
    }
}
