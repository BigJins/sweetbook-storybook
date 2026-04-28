package com.sweetbook.service.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockAiClientTest {

    private final MockAiClient ai = new MockAiClient();

    @Test
    void analyzeDrawingPopulatesAllFields() {
        StyleDescriptor d = ai.analyzeDrawing(new byte[]{1, 2}, "image/png");

        assertNotNull(d.subject(), "subject should not be null");
        assertNotNull(d.subjectType(), "subjectType should not be null");
        assertNotNull(d.mood(), "mood should not be null");
        assertFalse(d.keywords().isEmpty(), "keywords should not be empty");
        assertFalse(d.sceneCues().isEmpty(), "sceneCues should not be empty");
    }

    @Test
    void coverPageHasNullBodyAndDescribesSubject() {
        StyleDescriptor style = new StyleDescriptor(
            List.of("수채화풍"), "갈색 강아지", "ANIMAL", "따뜻한", List.of("공원"));
        StoryDraft draft = ai.generateStory("서아", "공원에서 함께 놀았어", style);

        StoryDraft.PageDraft cover = draft.pages().get(0);
        assertEquals(1, cover.pageNumber());
        assertNull(cover.bodyText(), "cover bodyText must be null per plan");
        assertTrue(cover.illustrationPrompt().contains("갈색 강아지"),
            "cover illustration should mention subject: " + cover.illustrationPrompt());
    }

    @Test
    void subjectAppearsInMostPagesNotChildName() {
        StyleDescriptor style = new StyleDescriptor(
            List.of("수채화풍"), "갈색 강아지", "ANIMAL", "따뜻한", List.of("공원", "꽃"));
        StoryDraft draft = ai.generateStory("서아", "공원에서 뛰어놀았어", style);

        long pagesMentioningSubject = draft.pages().stream()
            .filter(p -> p.illustrationPrompt() != null && p.illustrationPrompt().contains("갈색 강아지"))
            .count();
        assertTrue(pagesMentioningSubject >= 4,
            "subject should drive every page's illustration; got " + pagesMentioningSubject);
    }

    @Test
    void childNameIsIntegratedNaturallyNotForcedOnEveryPage() {
        StyleDescriptor style = new StyleDescriptor(
            List.of("수채화풍"), "갈색 강아지", "ANIMAL", "따뜻한", List.of("공원"));
        StoryDraft draft = ai.generateStory("서아", "함께 놀았어요", style);

        long bodyPagesMentioningChild = draft.pages().stream()
            .filter(p -> p.bodyText() != null)
            .filter(p -> p.bodyText().contains("서아"))
            .count();
        long bodyPages = draft.pages().stream()
            .filter(p -> p.bodyText() != null)
            .count();

        assertTrue(bodyPagesMentioningChild >= 1,
            "child name should appear at least once across body pages");
        assertTrue(bodyPagesMentioningChild < bodyPages,
            "child name should not be on every body page (forced); got "
                + bodyPagesMentioningChild + "/" + bodyPages);
    }

    @Test
    void titleReflectsSubjectNotChild() {
        StyleDescriptor style = new StyleDescriptor(
            List.of("수채화풍"), "갈색 강아지", "ANIMAL", "따뜻한", List.of("공원"));
        StoryDraft draft = ai.generateStory("서아", "공원", style);

        assertTrue(draft.title().contains("갈색 강아지"),
            "title should reference subject: " + draft.title());
        assertFalse(draft.title().contains("서아"),
            "title should not foreground child name in mock template: " + draft.title());
    }

    @Test
    void usesSubjectFallbackWhenAnalysisIsEmpty() {
        StoryDraft draft = ai.generateStory("서아", "어떤 모험", StyleDescriptor.empty());

        StoryDraft.PageDraft cover = draft.pages().get(0);
        assertTrue(cover.illustrationPrompt().contains("그림 속 주인공"),
            "should fall back to '그림 속 주인공' when subject missing: "
                + cover.illustrationPrompt());
    }

    @Test
    void bodyPagesHaveTwoToFourSentences() {
        StyleDescriptor style = new StyleDescriptor(
            List.of("수채화풍"), "갈색 강아지", "ANIMAL", "따뜻한", List.of("공원"));
        StoryDraft draft = ai.generateStory("서아", "공원에서 함께 놀았어요", style);

        for (int n = 2; n <= 4; n++) {
            StoryDraft.PageDraft page = draft.pages().get(n - 1);
            long sentenceCount = countSentences(page.bodyText());
            assertTrue(sentenceCount >= 2 && sentenceCount <= 4,
                "page " + n + " should have 2-4 sentences, got " + sentenceCount
                    + " in: " + page.bodyText());
        }
    }

    @Test
    void endingPageHasOneOrTwoSentences() {
        StyleDescriptor style = new StyleDescriptor(
            List.of("수채화풍"), "갈색 강아지", "ANIMAL", "따뜻한", List.of("공원"));
        StoryDraft draft = ai.generateStory("서아", "공원에서", style);

        long sentenceCount = countSentences(draft.pages().get(4).bodyText());
        assertTrue(sentenceCount >= 1 && sentenceCount <= 2,
            "ending should have 1-2 sentences, got " + sentenceCount);
    }

    private long countSentences(String text) {
        return text.chars().filter(c -> c == '.' || c == '!' || c == '?').count();
    }
}
