package com.sweetbook.service.ai;

import com.sweetbook.domain.story.PageLayout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class MockAiClient implements AiClient {

    private static final ThreadLocal<MockPreset> ACTIVE_PRESET = new ThreadLocal<>();

    @Override
    public StyleDescriptor analyzeDrawing(byte[] bytes, String contentType) {
        sleep(600);
        return new StyleDescriptor(
            List.of("아이 그림", "다채로운 색감", "동화책 스타일", "밝은 분위기"),
            "그림 속 주인공",
            "PERSON",
            "따뜻한",
            List.of("하늘", "꽃", "친구")
        );
    }

    @Override
    public StoryDraft generateStory(String childName, String imaginationPrompt, StyleDescriptor style) {
        sleep(600);
        MockPreset preset = MockPreset.pick(imaginationPrompt, style);
        ACTIVE_PRESET.set(preset);
        return preset.toDraft();
    }

    @Override
    public byte[] generateIllustration(String prompt, StyleDescriptor style, PageLayout layout, int pageNumber) {
        sleep(600);
        MockPreset preset = ACTIVE_PRESET.get();
        if (preset == null) {
            preset = MockPreset.pick(prompt, style);
        }
        try (InputStream in = new ClassPathResource(preset.resourceFor(pageNumber)).getInputStream()) {
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

    enum MockPreset {
        PRINCESS(
            "mock/presets/princess",
            new StyleDescriptor(
                List.of("어린이 그림", "다채로운 색상", "공주 의상선", "활기찬"),
                "왕관을 쓴 공주",
                "PERSON",
                "행복한",
                List.of("왕관", "드레스", "꽃", "왕국")
            ),
            "별빛 왕관의 하루",
            Arrays.asList(
                null,
                "반짝이는 왕관을 쓴 공주가 꽃밭 한가운데 서 있었어요. 작은 바람이 드레스를 살랑이며 오늘의 모험을 불러 주었답니다.",
                "공주는 알록달록한 정원을 걸으며 새로운 친구들을 만났어요. 모두 함께 웃자 햇살도 더 밝게 반짝였어요.",
                "조금 멀리 걷자 커다란 성문이 나타났어요. 공주는 두근두근한 마음으로 손을 흔들며 용감하게 앞으로 나아갔답니다.",
                "마지막으로 공주는 환하게 웃으며 하늘을 올려다보았어요. 오늘 하루가 오래도록 반짝이는 추억이 되었답니다."
            ),
            List.of(
                "왕관을 쓴 공주가 꽃밭과 푸른 하늘 앞에 서 있는 표지 장면.",
                "공주가 꽃이 가득한 정원에서 밝게 웃는 장면.",
                "공주가 친구들과 함께 정원을 걸어가는 장면.",
                "공주가 성문 앞에서 용감하게 손을 흔드는 장면.",
                "공주가 따뜻한 빛 아래에서 만족스럽게 미소 짓는 마무리 장면."
            )
        ),
        BEAR(
            "mock/presets/bear",
            new StyleDescriptor(
                List.of("자연주의", "부드러운 질감", "노란 별빛", "포근한 밤"),
                "미소 짓는 곰돌이",
                "ANIMAL",
                "포근한",
                List.of("들판", "별", "숲", "산책")
            ),
            "곰돌이와 별빛 산책",
            Arrays.asList(
                null,
                "작은 곰돌이가 들판 위에 앉아 반짝이는 별을 올려다보았어요. 오늘은 별빛을 따라 천천히 산책을 나가 보기로 했답니다.",
                "곰돌이는 꽃길을 지나며 밤하늘의 반짝임을 하나씩 세어 보았어요. 발걸음마다 풀잎이 살짝 흔들리고 마음도 한결 가벼워졌어요.",
                "조금 더 걷자 숲 가장자리에서 은은한 바람이 불어왔어요. 곰돌이는 커다란 하늘을 바라보며 조용히 소원을 떠올렸답니다.",
                "집으로 돌아오는 길에도 별빛은 곰돌이를 따라와 주었어요. 포근한 밤공기 속에서 오늘의 산책은 가장 빛나는 시간이 되었답니다."
            ),
            List.of(
                "별빛 아래에서 환하게 웃는 곰돌이의 표지 장면.",
                "곰돌이가 들판에서 별을 바라보는 장면.",
                "곰돌이가 꽃길과 풀숲 사이를 산책하는 장면.",
                "곰돌이가 숲 가장자리에서 하늘을 올려다보는 장면.",
                "곰돌이가 포근한 밤길을 따라 돌아가는 마무리 장면."
            )
        ),
        DOG(
            "mock/presets/tani",
            new StyleDescriptor(
                List.of("사진풍", "실내", "부드러운 조명", "포근한 분위기"),
                "까만 강아지",
                "ANIMAL",
                "포근한",
                List.of("낮잠", "침대", "방 안", "따뜻한 빛")
            ),
            "검은 강아지의 포근한 꿈",
            Arrays.asList(
                null,
                "까만 강아지가 포근한 이불 위에 몸을 동그랗게 말고 누웠어요. 따뜻한 햇살이 방 안을 감싸며 낮잠 시간이 시작되었답니다.",
                "강아지는 졸린 눈을 꿈뻑이며 조용히 숨을 골랐어요. 창밖의 바람 소리도 아주 부드럽게 들려왔어요.",
                "잠이 깊어질수록 강아지의 꿈속에는 반짝이는 별빛이 번졌어요. 포근한 품처럼 따뜻한 빛이 곁을 가만히 지켜 주었답니다.",
                "한참 뒤 강아지는 편안한 얼굴로 다시 눈을 떴어요. 잘 쉬고 난 오늘의 낮잠은 가장 다정한 꿈이 되었답니다."
            ),
            List.of(
                "포근한 이불 위에서 쉬고 있는 검은 강아지의 표지 장면.",
                "검은 강아지가 침대 위에서 눈을 반쯤 감고 쉬는 장면.",
                "검은 강아지가 조용한 방 안에서 낮잠에 빠져드는 장면.",
                "검은 강아지가 별빛 같은 꿈속 분위기 속에 있는 장면.",
                "검은 강아지가 낮잠에서 깨어나 편안하게 미소 짓는 마무리 장면."
            )
        );

        private final String resourceDir;
        private final StyleDescriptor style;
        private final String title;
        private final List<String> bodyTexts;
        private final List<String> illustrationPrompts;

        MockPreset(String resourceDir,
                   StyleDescriptor style,
                   String title,
                   List<String> bodyTexts,
                   List<String> illustrationPrompts) {
            this.resourceDir = resourceDir;
            this.style = style;
            this.title = title;
            this.bodyTexts = bodyTexts;
            this.illustrationPrompts = illustrationPrompts;
        }

        StoryDraft toDraft() {
            return new StoryDraft(title, List.of(
                page(1),
                page(2),
                page(3),
                page(4),
                page(5)
            ));
        }

        private StoryDraft.PageDraft page(int pageNumber) {
            return new StoryDraft.PageDraft(
                pageNumber,
                bodyTexts.get(pageNumber - 1),
                illustrationPrompts.get(pageNumber - 1)
            );
        }

        String resourceFor(int pageNumber) {
            if (pageNumber <= 1) {
                return resourceDir + "/cover.png";
            }
            return resourceDir + "/page-" + pageNumber + ".png";
        }

        static MockPreset pick(String text, StyleDescriptor style) {
            String normalized = normalize(text)
                + " "
                + normalize(style.subject())
                + " "
                + normalize(style.subjectType())
                + " "
                + normalize(style.mood())
                + " "
                + normalize(String.join(" ", style.keywords()))
                + " "
                + normalize(String.join(" ", style.sceneCues()));

            if (containsAny(normalized, "공주", "왕자", "왕국", "드레스", "왕관")) {
                return PRINCESS;
            }
            if (containsAny(normalized, "곰", "별", "숲", "토끼")) {
                return BEAR;
            }
            if (containsAny(normalized, "강아지", "낮잠", "포메", "검은")) {
                return DOG;
            }
            if ("PERSON".equals(style.subjectType())) {
                return PRINCESS;
            }
            return DOG;
        }

        private static boolean containsAny(String text, String... tokens) {
            for (String token : tokens) {
                if (text.contains(token)) {
                    return true;
                }
            }
            return false;
        }

        private static String normalize(String s) {
            return s == null ? "" : s.toLowerCase(Locale.ROOT);
        }
    }
}
