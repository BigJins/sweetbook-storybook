package com.sweetbook.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyleDescriptorTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void deserializesOldFormatWithKeywordsOnly() throws Exception {
        StyleDescriptor d = om.readValue(
            "{\"keywords\":[\"수채화풍\",\"파스텔톤\"]}",
            StyleDescriptor.class);

        assertEquals(List.of("수채화풍", "파스텔톤"), d.keywords());
        assertNull(d.subject());
        assertNull(d.subjectType());
        assertNull(d.mood());
        assertNotNull(d.sceneCues());
        assertTrue(d.sceneCues().isEmpty());
    }

    @Test
    void deserializesNewFormatWithAllFields() throws Exception {
        StyleDescriptor d = om.readValue(
            "{\"keywords\":[\"수채화풍\"],\"subject\":\"갈색 강아지\","
                + "\"subjectType\":\"ANIMAL\",\"mood\":\"따뜻한\","
                + "\"sceneCues\":[\"공원\",\"꽃\"]}",
            StyleDescriptor.class);

        assertEquals("갈색 강아지", d.subject());
        assertEquals("ANIMAL", d.subjectType());
        assertEquals("따뜻한", d.mood());
        assertEquals(List.of("공원", "꽃"), d.sceneCues());
    }

    @Test
    void deserializesIgnoresUnknownFields() throws Exception {
        StyleDescriptor d = om.readValue(
            "{\"keywords\":[\"a\"],\"futureField\":\"x\"}",
            StyleDescriptor.class);
        assertEquals(List.of("a"), d.keywords());
    }

    @Test
    void serializesOmitsNullFields() throws Exception {
        StyleDescriptor d = new StyleDescriptor(List.of("k"), null, null, null, null);
        String json = om.writeValueAsString(d);
        assertTrue(json.contains("\"keywords\""));
        assertTrue(json.contains("\"sceneCues\""));
        assertTrue(!json.contains("\"subject\""), "null subject should not be serialized: " + json);
        assertTrue(!json.contains("\"mood\""), "null mood should not be serialized: " + json);
    }

    @Test
    void asPromptPrefixJoinsKeywords() {
        StyleDescriptor d = new StyleDescriptor(
            List.of("수채화풍", "파스텔"), null, null, null, null);
        assertEquals("수채화풍, 파스텔", d.asPromptPrefix());
    }

    @Test
    void fallbacksUseDefaultsWhenBlank() {
        StyleDescriptor d = new StyleDescriptor(List.of(), null, null, null, null);
        assertEquals("그림 속 주인공", d.subjectOrFallback());
        assertEquals("따뜻한", d.moodOrFallback());

        StyleDescriptor empty = StyleDescriptor.empty();
        assertNotNull(empty.keywords());
        assertNotNull(empty.sceneCues());
    }
}
