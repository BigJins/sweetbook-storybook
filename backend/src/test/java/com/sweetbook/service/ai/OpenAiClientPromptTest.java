package com.sweetbook.service.ai;

import com.sweetbook.domain.story.PageLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiClientPromptTest {

    private final StyleDescriptor style = new StyleDescriptor(
        List.of("수채화풍", "파스텔톤"),
        "갈색 강아지",
        "ANIMAL",
        "따뜻한",
        List.of("공원", "꽃")
    );

    @Test
    void promptForbidsAllTypographyExhaustively() {
        String p = OpenAiClient.buildIllustrationPrompt(
            "강아지가 공원을 걷는 장면", style, PageLayout.SPLIT);

        assertTrue(p.contains("NO text"), "must say NO text");
        assertTrue(p.contains("NO letters"), "must say NO letters");
        assertTrue(p.contains("Korean characters"), "must explicitly forbid 한글");
        assertTrue(p.contains("한글"), "must forbid 한글 in Korean too");
        assertTrue(p.contains("NO alphabet"), "must forbid alphabet");
        assertTrue(p.contains("NO numbers"), "must forbid numbers");
        assertTrue(p.contains("NO words"), "must forbid words");
        assertTrue(p.contains("NO captions"), "must forbid captions");
        assertTrue(p.contains("NO speech bubbles"), "must forbid speech bubbles");
        assertTrue(p.contains("NO signs") || p.contains("NO signage"), "must forbid signage");
        assertTrue(p.contains("NO logos"), "must forbid logos");
        assertTrue(p.contains("NO watermarks"), "must forbid watermarks");
        assertTrue(p.contains("NO signatures"), "must forbid signatures");
        assertTrue(p.contains("wordless"), "must say image is wordless");
    }

    @Test
    void promptVariesTextSafeAreaByLayout() {
        String cover = OpenAiClient.buildIllustrationPrompt("표지", style, PageLayout.COVER);
        String split = OpenAiClient.buildIllustrationPrompt("본문 장면", style, PageLayout.SPLIT);
        String ending = OpenAiClient.buildIllustrationPrompt("엔딩", style, PageLayout.ENDING);

        assertTrue(cover.contains("COVER"), "cover prompt must mention COVER composition");
        assertTrue(cover.contains("title-safe"), "cover prompt must mention title-safe zone");

        assertTrue(split.contains("INTERIOR") || split.contains("body-text-safe"),
            "split prompt must hint at body-text-safe area");
        assertTrue(split.contains("right side") && split.contains("lower third"),
            "split prompt should offer right-side or lower-third positioning");

        assertTrue(ending.contains("CLOSING") || ending.contains("restful"),
            "ending prompt must feel calm/closing");
        assertTrue(ending.contains("negative space"),
            "ending prompt must mention open negative space");

        assertNotEquals(cover, split, "cover and split prompts must differ");
        assertNotEquals(split, ending, "split and ending prompts must differ");
    }

    @Test
    void promptIncludesSubjectMoodAndScene() {
        String p = OpenAiClient.buildIllustrationPrompt(
            "공원에서 공을 따라 달리는 장면", style, PageLayout.SPLIT);

        assertTrue(p.contains("갈색 강아지"), "must include subject");
        assertTrue(p.contains("ANIMAL"), "must include subjectType");
        assertTrue(p.contains("따뜻한"), "must include mood");
        assertTrue(p.contains("공원"), "must include scene cues");
        assertTrue(p.contains("공원에서 공을 따라 달리는 장면"), "must include the page scene prompt");
        assertTrue(p.contains("수채화풍"), "must include style keywords");
    }

    @Test
    void promptKeepsCorePictureBookFormat() {
        String p = OpenAiClient.buildIllustrationPrompt("장면", style, PageLayout.SPLIT);
        assertTrue(p.contains("4:5"), "must specify 4:5 aspect");
        assertTrue(p.contains("어린이 그림책"), "must specify children's picture book");
        assertTrue(p.contains("주인공이 화면 안에 명확히"), "must keep subject-clear instruction");
    }

    @Test
    void promptToleratesEmptyStyle() {
        String p = OpenAiClient.buildIllustrationPrompt(
            "장면", StyleDescriptor.empty(), PageLayout.SPLIT);
        // No NPE; falls back to "그림 속 주인공"
        assertTrue(p.contains("그림 속 주인공"));
    }

    @Test
    void promptToleratesNullLayout() {
        String p = OpenAiClient.buildIllustrationPrompt("장면", style, null);
        // Default to SPLIT framing if layout is null
        assertTrue(p.contains("INTERIOR") || p.contains("body-text-safe"));
    }

    @Test
    void illustrationPromptForbidsSubjectInsideTextSafeZone() {
        String p = OpenAiClient.buildIllustrationPrompt("장면", style, PageLayout.SPLIT);
        assertTrue(p.contains("must NOT") && p.contains("text-safe zone"),
            "must explicitly forbid subject sitting inside the text-safe zone");
        assertTrue(p.contains("opposite portion") || p.contains("opposite side"),
            "should redirect important elements to the opposite portion");
    }

    @Test
    void coverPromptIsSinglePageNotSpread() {
        String cover = squish(OpenAiClient.buildIllustrationPrompt("표지", style, PageLayout.COVER));
        assertTrue(cover.contains("SINGLE FRONT PAGE"),
            "cover must be framed as a single front page");
        assertTrue(cover.contains("not a two-page spread") || cover.contains("closed cover"),
            "cover should explicitly contrast itself with a spread");
    }

    @Test
    void splitPromptFramesImageAsOneSideOfOpenSpread() {
        String split = squish(OpenAiClient.buildIllustrationPrompt("본문 장면", style, PageLayout.SPLIT));
        assertTrue(split.contains("ONE SIDE"),
            "split prompt must say ONE SIDE: " + split);
        assertTrue(split.contains("OPEN-BOOK SPREAD"),
            "split prompt must mention OPEN-BOOK SPREAD: " + split);
        assertTrue(split.contains("companion page") || split.contains("half-spread"),
            "split prompt should mention the companion / half-spread context");
        assertTrue(split.contains("DO NOT crop"),
            "split prompt must warn against cropping subject at inner edge");
        assertTrue(split.contains("page-margin") || split.contains("page margin"),
            "text-safe area should be described as a page margin, not just blank space");
    }

    @Test
    void endingPromptFramesImageAsOneSideOfSpread() {
        String ending = squish(OpenAiClient.buildIllustrationPrompt("엔딩", style, PageLayout.ENDING));
        assertTrue(ending.contains("ONE SIDE"),
            "ending prompt should also acknowledge it sits as one side of the spread");
        assertTrue(ending.contains("page-margin") || ending.contains("page margin"),
            "ending text-safe area should be called a margin");
        assertTrue(ending.contains("never in the lower margin"),
            "ending prompt must say subject doesn't sit in the lower margin");
    }

    @Test
    void globalImportantLineMentionsPageMargin() {
        String p = squish(OpenAiClient.buildIllustrationPrompt("장면", style, PageLayout.SPLIT));
        assertTrue(p.contains("page margin") || p.contains("page-margin"),
            "global Important: line should describe the safe zone as a page margin");
        assertTrue(p.contains("never on top of where the text will land"),
            "global rule should explicitly forbid placing subject on top of the text area");
    }

    private static String squish(String s) {
        return s.replaceAll("\\s+", " ");
    }

    @Test
    void storySystemPromptIncludesTextLengthRules() {
        String p = OpenAiClient.STORY_SYSTEM_PROMPT;

        assertTrue(p.contains("70~110"),
            "should specify ~70-110 char range per body page");
        assertTrue(p.contains("2~4"),
            "should specify 2-4 sentences per body page");
        assertTrue(p.contains("1~2문장"),
            "should specify 1-2 sentence ending");
        assertTrue(p.contains("반복"),
            "should warn against repeated phrasing");
        assertTrue(p.contains("리듬") || p.contains("호흡"),
            "should mention picture-book rhythm/breath");
        assertTrue(p.contains("짧"),
            "should prefer shorter writing");
        assertTrue(p.contains("오버레이"),
            "should mention text is overlaid by the layout system");
    }
}
