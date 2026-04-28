package com.sweetbook.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:ord;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.locations=classpath:db/migration",
    "app.upload-dir=./build/ord-uploads",
    "app.ai.mock-mode=true"
})
class OrderControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void createsOrderAndProgressesForward() throws Exception {
        String body = """
            {"storyId":"seed-story-1","bookSize":"A5","coverType":"HARD",
             "copies":1,"recipientName":"테스터","addressMemo":""}""";
        var result = mvc.perform(post("/api/orders").contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.story.id").value("seed-story-1"))
            .andExpect(jsonPath("$.item.bookSize").value("A5"))
            .andReturn();
        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mvc.perform(patch("/api/orders/" + id + "/status").contentType("application/json")
                .content("{\"status\":\"PROCESSING\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void rejectsInvalidTransition() throws Exception {
        mvc.perform(patch("/api/orders/seed-order-1/status").contentType("application/json")
                .content("{\"status\":\"PENDING\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("INVALID_TRANSITION"));
    }

    @Test
    void exportEndpointReturnsZip() throws Exception {
        var mvcResult = mvc.perform(get("/api/orders/seed-order-1/export"))
            .andExpect(request().asyncStarted())
            .andReturn();

        mvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/zip"))
            .andExpect(header().string("Content-Disposition",
                containsString("order-seed-order-1.zip")));
    }
}
