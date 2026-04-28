-- Story 1: 곰돌이의 별 따기
INSERT INTO story (id, title, child_name, status, drawing_url, style_descriptor, imagination_prompt, created_at, updated_at)
VALUES ('seed-story-1', '곰돌이의 별 따기', '서아', 'COMPLETED',
        'seed/story-1/drawing.png',
        '{"keywords":["수채화풍","따뜻한 파스텔","굵은 외곽선","천진한 동물 캐릭터"]}',
        '곰돌이가 우주에 가서 별을 따왔어!',
        '2026-04-28 10:00:00.000', '2026-04-28 10:00:00.000');

INSERT INTO page (id, story_id, page_number, layout, body_text, illustration_prompt, illustration_url) VALUES
('seed-page-1-1', 'seed-story-1', 1, 'COVER',  NULL, '곰돌이가 우주선 옆에 서서 웃는 표지', 'seed/story-1/cover.png'),
('seed-page-1-2', 'seed-story-1', 2, 'SPLIT',  '곰돌이는 살금살금 우주선에 올라탔어요. 창밖으로 별들이 반짝반짝 인사를 했답니다.', '우주선 안의 곰돌이', 'seed/story-1/page-2.png'),
('seed-page-1-3', 'seed-story-1', 3, 'SPLIT',  '우주선은 점점 더 높이 올라갔어요. 지구가 작은 공처럼 보였답니다.', '우주에서 본 지구', 'seed/story-1/page-3.png'),
('seed-page-1-4', 'seed-story-1', 4, 'SPLIT',  '드디어 별 한 송이를 손에 잡았어요! 별은 따뜻하고 부드러웠어요.', '별을 잡은 곰돌이', 'seed/story-1/page-4.png'),
('seed-page-1-5', 'seed-story-1', 5, 'ENDING', '곰돌이는 별을 가지고 집으로 돌아왔어요. 그날 밤, 곰돌이의 방은 가장 환했답니다.', '집에 돌아온 곰돌이', 'seed/story-1/page-5.png');

-- Story 2: 유니콘과 무지개 다리
INSERT INTO story (id, title, child_name, status, drawing_url, style_descriptor, imagination_prompt, created_at, updated_at)
VALUES ('seed-story-2', '유니콘과 무지개 다리', '하윤', 'COMPLETED',
        'seed/story-2/drawing.png',
        '{"keywords":["꿈결같은 파스텔","반짝이는 디테일","부드러운 곡선"]}',
        '유니콘이 무지개를 타고 학교에 같이 가는 이야기',
        '2026-04-28 10:05:00.000', '2026-04-28 10:05:00.000');

INSERT INTO page (id, story_id, page_number, layout, body_text, illustration_prompt, illustration_url) VALUES
('seed-page-2-1', 'seed-story-2', 1, 'COVER',  NULL, '무지개 위 유니콘 표지', 'seed/story-2/cover.png'),
('seed-page-2-2', 'seed-story-2', 2, 'SPLIT',  '아침 햇살이 창문을 두드렸어요. 하윤이는 가방을 메고 문을 열었답니다.', '책가방 멘 아이', 'seed/story-2/page-2.png'),
('seed-page-2-3', 'seed-story-2', 3, 'SPLIT',  '문 앞에 유니콘이 기다리고 있었어요. "오늘은 무지개를 타고 가자!"', '유니콘이 부르는 장면', 'seed/story-2/page-3.png'),
('seed-page-2-4', 'seed-story-2', 4, 'SPLIT',  '둘은 무지개 다리를 폴짝폴짝 건넜어요. 발밑에서 색깔이 톡톡 튀었답니다.', '무지개 다리', 'seed/story-2/page-4.png'),
('seed-page-2-5', 'seed-story-2', 5, 'ENDING', '학교 종이 울렸어요. 친구들이 와! 하고 환호했답니다.', '학교 도착', 'seed/story-2/page-5.png');

-- Story 3: 파란 고래와 수영하기
INSERT INTO story (id, title, child_name, status, drawing_url, style_descriptor, imagination_prompt, created_at, updated_at)
VALUES ('seed-story-3', '파란 고래와 수영하기', '지호', 'COMPLETED',
        'seed/story-3/drawing.png',
        '{"keywords":["수채 바다","청량한 톤"]}',
        '커다란 파란 고래와 바다에서 수영',
        '2026-04-28 10:10:00.000', '2026-04-28 10:10:00.000');

INSERT INTO page (id, story_id, page_number, layout, body_text, illustration_prompt, illustration_url) VALUES
('seed-page-3-1', 'seed-story-3', 1, 'COVER',  NULL, '고래와 아이 표지', 'seed/story-3/cover.png'),
('seed-page-3-2', 'seed-story-3', 2, 'SPLIT',  '바다 한가운데, 커다란 그림자가 다가왔어요.', '바다 그림자', 'seed/story-3/page-2.png'),
('seed-page-3-3', 'seed-story-3', 3, 'SPLIT',  '고래는 등을 살짝 내밀어 지호를 태웠어요.', '고래 등에 탄 아이', 'seed/story-3/page-3.png'),
('seed-page-3-4', 'seed-story-3', 4, 'SPLIT',  '둘은 산호 사이를 천천히 지나갔어요.', '산호 정원', 'seed/story-3/page-4.png'),
('seed-page-3-5', 'seed-story-3', 5, 'ENDING', '해질녘, 고래는 지호를 모래사장에 살며시 내려놓았어요.', '석양 해변', 'seed/story-3/page-5.png');

-- Story 4: 탄이의 낮잠 이야기
INSERT INTO story (id, title, child_name, status, drawing_url, style_descriptor, imagination_prompt, created_at, updated_at)
VALUES ('seed-story-4', '탄이의 낮잠 이야기', '서아', 'COMPLETED',
        'seed/story-4/drawing.png',
        '{"mood":"포근한","subject":"검은 포메라니안","keywords":["실내촬영","또렷한 디테일","자연스러운 포즈"],"sceneCues":["책상","모니터","사진"],"subjectType":"ANIMAL"}',
        '강아지가 주인공, 강아지 이름은 탄이, 포메, 탄이가 낮잠을 많이잔다. 탄이의 주인인 서아(여자아이)는 탄이가 낮잠을 많이 자는게 섭섭하고, 왜그런지 궁금해. 탄이는 서아를 좋아해서 밤늦게 서아를 지키느라 힘들었던거. 서아가 탄이의 입장을 생각하게 되는 동화',
        '2026-04-28 10:15:00.000', '2026-04-28 10:15:00.000');

INSERT INTO page (id, story_id, page_number, layout, body_text, illustration_prompt, illustration_url) VALUES
('seed-page-4-1', 'seed-story-4', 1, 'COVER',  NULL, '검은 포메라니안 탄이가 침대 위에서 편안하게 낮잠을 자고 있는 모습. 서아가 책상 옆에 앉아 탄이를 바라보며 궁금해하는 표정.', 'seed/story-4/cover.png'),
('seed-page-4-2', 'seed-story-4', 2, 'SPLIT',  '서아는 탄이가 낮잠을 많이 자는 것이 아쉬워. "왜 이렇게 자는 걸까?" 서아는 곰곰이 생각해봤어.', '서아가 책상에 앉아 탄이를 바라보며 고민하는 모습. 주변에는 색색의 장난감과 쿠션이 흩어져 있어.', 'seed/story-4/page-2.png'),
('seed-page-4-3', 'seed-story-4', 3, 'SPLIT',  '그때, 서아는 탄이를 보며 말해봤어. "탄아, 할 일 없니?" 탄이는 낮잠을 자며 꿈속에서 서아를 지키고 있었어.', '탄이가 꿈속에서 서아를 지키는 환상적인 장면, 별빛이 반짝이는 배경 속에서 탄이가 용감하게 서아를 보호하는 모습.', 'seed/story-4/page-3.png'),
('seed-page-4-4', 'seed-story-4', 4, 'SPLIT',  '서아는 이해했어. 탄이는 늦은 밤, 항상 서아를 지키기 위해 힘들었구나. "고마워, 탄아!"라고 말했어.', '서아가 탄이를 껴안으며 웃고 있는 모습. 주변은 따뜻한 빛으로 가득 차 있고, 사랑스러운 분위기가 감돌고 있어.', 'seed/story-4/page-4.png'),
('seed-page-4-5', 'seed-story-4', 5, 'ENDING', '탄이와 서아는 서로를 바라보며 행복해했어. 이제 서로의 마음을 다 알게 되었으니까.', '탄이와 서아가 서로를 바라보며 미소 짓고 있는 따뜻한 장면. 배경에는 저녁 노을이 비치고, 평화로운 분위기가 느껴짐.', 'seed/story-4/page-5.png');

-- 시드 주문 1건 (PROCESSING 상태)
INSERT INTO orders (id, story_id, recipient_name, address_memo, status, status_history, created_at, updated_at)
VALUES ('seed-order-1', 'seed-story-1', '김서아', '시연용 데이터', 'PROCESSING',
        '[{"status":"PENDING","ts":"2026-04-28T10:00:00Z"},{"status":"PROCESSING","ts":"2026-04-28T11:00:00Z"}]',
        '2026-04-28 10:00:00.000', '2026-04-28 11:00:00.000');

INSERT INTO order_item (id, order_id, book_size, cover_type, copies)
VALUES ('seed-item-1', 'seed-order-1', 'A5', 'HARD', 1);
