package com.sweetbook.service;

import com.sweetbook.domain.story.Story;
import com.sweetbook.domain.story.StoryStatus;
import com.sweetbook.repository.PageRepository;
import com.sweetbook.repository.StoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:gentest;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.locations=classpath:db/migration",
    "app.upload-dir=./build/gentest-uploads",
    "app.ai.mock-mode=true"
})
class StoryGenerationServiceTest {

    @Autowired
    StoryGenerationService gen;

    @Autowired
    StoryRepository stories;

    @Autowired
    PageRepository pages;

    @Autowired
    FileStorageService storage;

    @Test
    void mockGenerationCompletes() throws Exception {
        var drawing = new MockMultipartFile("d", "k.png", "image/png", "PNG".getBytes());
        Story s = Story.newDraft("테스트", "용기 있는 곰돌이가 모험을 떠난다");
        s.setDrawingUrl(storage.saveDrawing(drawing));
        stories.save(s);
        String id = s.getId();

        gen.generate(id);
        Thread.sleep(5000);

        Story after = stories.findById(id).orElseThrow();
        assertEquals(StoryStatus.COMPLETED, after.getStatus());
        assertNotNull(after.getStyleDescriptorJson());

        long pageCount = java.util.stream.IntStream.rangeClosed(1, 5)
            .filter(n -> pages.findByStoryIdAndPageNumber(id, n).isPresent())
            .count();
        assertEquals(5, pageCount);
    }
}
