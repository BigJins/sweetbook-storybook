package com.sweetbook.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:createtest;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.locations=classpath:db/migration",
    "app.upload-dir=./build/createtest-uploads",
    "app.ai.mock-mode=true"
})
class StoryControllerCreateTest {

    @Autowired
    MockMvc mvc;

    @Test
    void rejectsShortImagination() throws Exception {
        var drawing = new MockMultipartFile("drawing", "k.png", "image/png", "PNG".getBytes());
        mvc.perform(multipart("/api/stories")
                .file(drawing)
                .param("childName", "서아")
                .param("imaginationPrompt", "짧음"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.message").value(containsString("10자 이상")));
    }

    @Test
    void rejectsGifImage() throws Exception {
        var drawing = new MockMultipartFile("drawing", "k.gif", "image/gif", "GIF".getBytes());
        mvc.perform(multipart("/api/stories")
                .file(drawing)
                .param("childName", "서아")
                .param("imaginationPrompt", "곰돌이가 우주에 가서 별을 따왔어요"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("JPG/PNG")));
    }

    @Test
    void createsDraftStoryOnHappyPath() throws Exception {
        var drawing = new MockMultipartFile("drawing", "k.png", "image/png", "PNG_BYTES".getBytes());
        mvc.perform(multipart("/api/stories")
                .file(drawing)
                .param("childName", "서아")
                .param("imaginationPrompt", "곰돌이가 우주에 가서 별을 따왔어요"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("DRAFT"));
    }
}
