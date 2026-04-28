package com.sweetbook.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:spa;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.locations=classpath:db/migration",
    "app.upload-dir=./build/spa-uploads",
    "app.ai.mock-mode=true"
})
class SpaFallbackTest {

    @Autowired
    MockMvc mvc;

    @Test
    void rootForwardsToIndex() throws Exception {
        mvc.perform(get("/")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void storyDetailRouteForwardsToIndex() throws Exception {
        mvc.perform(get("/stories/seed-story-1")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void newStoryRouteForwardsToIndex() throws Exception {
        mvc.perform(get("/stories/new")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void ordersRouteForwardsToIndex() throws Exception {
        mvc.perform(get("/orders")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void orderDetailRouteForwardsToIndex() throws Exception {
        mvc.perform(get("/orders/seed-order-1")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void apiRoutesAreNotForwarded() throws Exception {
        mvc.perform(get("/api/stories")).andExpect(status().isOk());
    }
}
