package com.sweetbook.service.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class MockAiClient implements AiClient {

    @Override
    public StyleDescriptor analyzeDrawing(byte[] bytes, String contentType) {
        sleep(600);
        return new StyleDescriptor(
            List.of("수채화풍", "따뜻한 파스텔", "굵은 외곽선", "천진한 캐릭터"),
            "곰돌이",
            "ANIMAL",
            "따뜻한",
            List.of("우주", "별", "작은 우주선")
        );
    }

    @Override
    public StoryDraft generateStory(String childName, String prompt, StyleDescriptor style) {
        sleep(600);
        String subject = style.subjectOrFallback();
        String mood = style.moodOrFallback();
        String firstCue = style.sceneCues().isEmpty() ? "넓은 들판" : style.sceneCues().get(0);

        String title = "우리 " + subject + "의 모험";

        return new StoryDraft(title, List.of(
            new StoryDraft.PageDraft(1, null,
                "표지: " + subject + "이 " + firstCue + "에서 활짝 웃고 있는 한 컷, " + mood + " 분위기"),
            new StoryDraft.PageDraft(2,
                childName + "이 그린 " + subject + "이 어느 날 그림 밖으로 살며시 걸어 나왔어요. "
                    + "두 눈을 깜빡깜빡, 처음 보는 세상이 신기했답니다.",
                subject + "이 종이에서 걸어 나오는 장면, " + firstCue + " 배경"),
            new StoryDraft.PageDraft(3,
                subject + "은 용기를 내어 " + prompt + " "
                    + "발걸음이 떨렸지만 가슴은 두근두근 뛰었어요.",
                subject + "이 모험을 시작하는 장면"),
            new StoryDraft.PageDraft(4,
                "그러다 " + subject + "은 " + childName + "이를 다시 만났어요. "
                    + "\"같이 가자!\" 둘은 손을 잡고 더 멀리 떠났답니다.",
                subject + "과 아이가 함께 모험하는 장면"),
            new StoryDraft.PageDraft(5,
                "긴 하루가 저물 무렵, " + subject + "은 " + childName + "이의 그림책 속으로 살며시 돌아갔어요. "
                    + "내일 또 만나기로 약속하면서, 둘 다 " + mood + " 미소를 지었답니다.",
                subject + "이 그림책으로 돌아가는 따뜻한 엔딩 장면")
        ));
    }

    @Override
    public byte[] generateIllustration(String prompt, StyleDescriptor style) {
        sleep(600);
        try (InputStream in = new ClassPathResource("seed/placeholder.png").getInputStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
