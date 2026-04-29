package com.sweetbook.service.ai;

import com.sweetbook.domain.story.PageLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockAiClientTest {

    private final MockAiClient ai = new MockAiClient();

    @Test
    void analyzeDrawingReturnsUsableFallbackStyle() {
        StyleDescriptor d = ai.analyzeDrawing(new byte[]{1, 2}, "image/png");

        assertNotNull(d.subject());
        assertNotNull(d.subjectType());
        assertNotNull(d.mood());
        assertTrue(d.keywords().size() >= 3);
        assertTrue(d.sceneCues().size() >= 2);
    }

    @Test
    void princessPromptSelectsPrincessPreset() {
        StoryDraft draft = ai.generateStory(
            "서아",
            "공주님과 왕자님이 만나 행복하게 사는 이야기",
            StyleDescriptor.empty()
        );

        assertEquals("서아의 행복한 왕국", draft.title());
        assertNull(draft.pages().get(0).bodyText());
        assertTrue(draft.pages().get(1).bodyText().contains("공주"));
    }

    @Test
    void bearPromptSelectsBearPreset() {
        StoryDraft draft = ai.generateStory(
            "서아",
            "곰돌이가 별을 따러 가는 여행 이야기",
            StyleDescriptor.empty()
        );

        assertEquals("곰돌이의 별 따러 가는 여행", draft.title());
        assertTrue(draft.pages().get(2).illustrationPrompt().contains("곰돌이"));
    }

    @Test
    void dogPromptSelectsTaniPresetAndReplacesChildName() {
        StoryDraft draft = ai.generateStory(
            "민지",
            "강아지 탄이가 낮잠을 많이 자는 이유가 궁금한 이야기",
            StyleDescriptor.empty()
        );

        assertEquals("탄이의 낮잠 이야기", draft.title());
        assertTrue(draft.pages().get(1).bodyText().contains("민지"));
        assertTrue(draft.pages().get(4).bodyText().contains("민지"));
    }

    @Test
    void generateIllustrationReturnsPresetImageBytesPerPage() {
        ai.generateStory("서아", "공주와 왕자의 이야기", StyleDescriptor.empty());

        byte[] cover = ai.generateIllustration("표지", StyleDescriptor.empty(), PageLayout.COVER, 1);
        byte[] page2 = ai.generateIllustration("본문", StyleDescriptor.empty(), PageLayout.SPLIT, 2);

        assertTrue(cover.length > 1000);
        assertTrue(page2.length > 1000);
        assertTrue(cover.length != page2.length);
    }

    @Test
    void bodyPagesStayWithinTwoToFourSentences() {
        StoryDraft draft = ai.generateStory(
            "서아",
            "공주님과 왕자님이 행복하게 사는 이야기",
            StyleDescriptor.empty()
        );

        for (int n = 2; n <= 4; n++) {
            long sentenceCount = countSentences(draft.pages().get(n - 1).bodyText());
            assertTrue(sentenceCount >= 2 && sentenceCount <= 4);
        }
    }

    @Test
    void endingPageUsesOneOrTwoSentences() {
        StoryDraft draft = ai.generateStory(
            "서아",
            "곰돌이가 별을 따러 가는 여행 이야기",
            StyleDescriptor.empty()
        );

        long sentenceCount = countSentences(draft.pages().get(4).bodyText());
        assertTrue(sentenceCount >= 1 && sentenceCount <= 2);
    }

    @Test
    void styleCanStillGuideFallbackSelection() {
        StoryDraft draft = ai.generateStory(
            "서아",
            "",
            new StyleDescriptor(
                List.of("동화풍"),
                "갈색 곰",
                "ANIMAL",
                "따뜻한",
                List.of("숲", "별")
            )
        );

        assertEquals("곰돌이의 별 따러 가는 여행", draft.title());
    }

    private long countSentences(String text) {
        return text.chars().filter(c -> c == '.' || c == '!' || c == '?').count();
    }
}
