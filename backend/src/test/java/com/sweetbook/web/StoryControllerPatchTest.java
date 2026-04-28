package com.sweetbook.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:patchtest;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.locations=classpath:db/migration",
    "app.upload-dir=./build/patchtest-uploads",
    "app.ai.mock-mode=true"
})
class StoryControllerPatchTest {

    @Autowired
    MockMvc mvc;

    @Test
    void updatesPageBody() throws Exception {
        mvc.perform(patch("/api/stories/seed-story-1/pages/2")
                .contentType("application/json")
                .content("{\"bodyText\":\"새 본문이에요\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true));

        mvc.perform(get("/api/stories/seed-story-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pages[1].bodyText").value("새 본문이에요"));
    }
}
