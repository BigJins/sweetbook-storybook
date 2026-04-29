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
    void princessPromptSelectsDedicatedMockPreset() {
        StoryDraft draft = ai.generateStory(
            "서아",
            "공주님과 왕자님이 만나 행복하게 사는 이야기",
            StyleDescriptor.empty()
        );

        assertEquals("별빛 왕관의 하루", draft.title());
        assertNull(draft.pages().get(0).bodyText());
        assertTrue(draft.pages().get(1).bodyText().contains("공주"));
        assertFalseContains(draft.pages().get(1).bodyText(), "서아");
        assertFalseContains(draft.title(), "서아의 행복한 왕국");
    }

    @Test
    void bearPromptSelectsDedicatedMockPreset() {
        StoryDraft draft = ai.generateStory(
            "민지",
            "곰돌이가 별을 따러 가는 여행 이야기",
            StyleDescriptor.empty()
        );

        assertEquals("곰돌이와 별빛 산책", draft.title());
        assertTrue(draft.pages().get(2).illustrationPrompt().contains("곰돌이"));
        assertFalseContains(draft.title(), "곰돌이의 별 따러 가는 여행");
    }

    @Test
    void dogPromptSelectsDedicatedMockPreset() {
        StoryDraft draft = ai.generateStory(
            "탄이",
            "강아지가 낮잠을 자며 꿈을 꾸는 이야기",
            StyleDescriptor.empty()
        );

        assertEquals("검은 강아지의 포근한 꿈", draft.title());
        assertTrue(draft.pages().get(1).bodyText().contains("강아지"));
        assertFalseContains(draft.title(), "탄이의 낮잠 이야기");
    }

    @Test
    void childNameDoesNotLeakIntoFixedMockStory() {
        StoryDraft draft = ai.generateStory(
            "아무개",
            "공주님과 왕국 이야기",
            StyleDescriptor.empty()
        );

        for (int i = 1; i < draft.pages().size(); i++) {
            String body = draft.pages().get(i).bodyText();
            if (body != null) {
                assertFalseContains(body, "아무개");
            }
        }
    }

    @Test
    void generateIllustrationReturnsPresetImageBytesPerPage() {
        ai.generateStory("서아", "공주와 왕자 이야기", StyleDescriptor.empty());

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
            "곰돌이가 별을 보며 산책하는 이야기",
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
                "포근한",
                List.of("숲", "별")
            )
        );

        assertEquals("곰돌이와 별빛 산책", draft.title());
    }

    private long countSentences(String text) {
        return text.chars().filter(c -> c == '.' || c == '!' || c == '?').count();
    }

    private void assertFalseContains(String text, String unexpected) {
        assertTrue(text == null || !text.contains(unexpected));
    }
}
