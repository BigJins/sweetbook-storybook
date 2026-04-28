-- Story 1: 곰돌이의 별 따러 가는 여행
INSERT INTO story (id, title, child_name, status, drawing_url, style_descriptor, imagination_prompt, created_at, updated_at)
VALUES ('seed-story-1', '곰돌이의 별 따러 가는 여행', '서아', 'COMPLETED',
        'seed/story-1/drawing.png',
        '{"mood":"귀여운","subject":"갈색 곰","keywords":["생동감있는","자연주의","부드러운 느낌"],"sceneCues":["풀밭","들꽃","숲"],"subjectType":"ANIMAL"}',
        '곰돌이가 별을따러가는 이야기. 곰돌이 가 이름이야. 애기곰이 별을 따러가는 여행 이야기.',
        '2026-04-28 10:00:00.000', '2026-04-28 10:00:00.000');

INSERT INTO page (id, story_id, page_number, layout, body_text, illustration_prompt, illustration_url) VALUES
('seed-page-1-1', 'seed-story-1', 1, 'COVER',  NULL, '갈색 곰돌이가 푸른 풀밭 위에 앉아 별을 바라보며 웃고 있다. 주변에는 알록달록한 들꽃들이 만발해 있다.', 'seed/story-1/cover.png'),
('seed-page-1-2', 'seed-story-1', 2, 'SPLIT',  '어느 날, 곰돌이는 밤하늘의 별을 보고 말했어요. "저 별들을 따러 가고 싶어!" 생각하며 숲 속으로 나섰어요.', '곰돌이가 숲길을 따라 걸으며 별을 향해 손을 내밀고 있다. 숲은 밝은 햇살이 비추고, 나무들은 푸르름으로 가득 차 있다.', 'seed/story-1/page-2.png'),
('seed-page-1-3', 'seed-story-1', 3, 'SPLIT',  '숲 속에서 다양한 친구들을 만났어요. 토끼가 말했어요. "같이 가줄게!" 곰돌이는 기뻐하며 답했어요. "고마워! 함께 가자!"', '곰돌이와 토끼가 함께 웃으며 걷고 있다. 주변에는 나비들이 날아다니고, 은은한 햇살이 내리쬐고 있다.', 'seed/story-1/page-3.png'),
('seed-page-1-4', 'seed-story-1', 4, 'SPLIT',  '드디어 높은 언덕에 도착했어요. 곰돌이는 손을 쭉 뻗어 별을 잡으려 했어요. "와! 너무 아름다워!" 친구들도 신기하게 바라봤어요.', '곰돌이가 높은 언덕에서 하늘을 향해 손을 뻗고 있고, 토끼와 여러 동물 친구들이 곰돌이를 바라보며 즐거워하고 있다.', 'seed/story-1/page-4.png'),
('seed-page-1-5', 'seed-story-1', 5, 'ENDING', '그날 밤, 곰돌이는 별빛 속에서 행복한 꿈을 꾸었어요. 별은 멀리 있었지만, 친구들과 함께라서 좋았어요.', '곰돌이가 풀밭에 누워 별빛을 바라보며 미소를 짓고 있다. 주변 친구들이 곁에 앉아있고, 밤하늘은 반짝이며 아름답다.', 'seed/story-1/page-5.png');

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
