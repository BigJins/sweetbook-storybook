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
        return new StyleDescriptor(List.of("수채화풍", "따뜻한 파스텔", "굵은 외곽선", "천진한 캐릭터"));
    }

    @Override
    public StoryDraft generateStory(String childName, String prompt, StyleDescriptor style) {
        sleep(600);
        return new StoryDraft("내가 만든 동화", List.of(
            new StoryDraft.PageDraft(1, null, "표지: " + prompt),
            new StoryDraft.PageDraft(2, childName + "의 모험이 시작되었어요. 모든 게 반짝반짝 빛났답니다.", "장면 1"),
            new StoryDraft.PageDraft(3, "조심스럽게 한 발 한 발 나아갔어요. 신비한 친구를 만났답니다.", "장면 2"),
            new StoryDraft.PageDraft(4, "함께 손을 잡고 더 멀리 떠났어요. 마법 같은 일이 펼쳐졌어요.", "장면 3"),
            new StoryDraft.PageDraft(5, "그렇게 " + childName + "이는 행복하게 잠들었답니다.", "엔딩 장면")
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
