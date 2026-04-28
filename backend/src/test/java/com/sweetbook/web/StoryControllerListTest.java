package com.sweetbook.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.locations=classpath:db/migration",
    "app.upload-dir=./build/test-uploads",
    "app.ai.mock-mode=true"
})
class StoryControllerListTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listsSeedStories() throws Exception {
        mvc.perform(get("/api/stories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].title").exists());
    }

    @Test
    void getDetailIncludesFivePages() throws Exception {
        mvc.perform(get("/api/stories/seed-story-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pages.length()").value(5))
            .andExpect(jsonPath("$.pages[0].layout").value("COVER"));
    }
}
