package com.sweetbook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbook.domain.order.Order;
import com.sweetbook.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:zip;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.locations=classpath:db/migration",
    "app.upload-dir=./build/zip-uploads",
    "app.ai.mock-mode=true"
})
class ZipExportServiceTest {

    @Autowired
    ZipExportService zipExporter;

    @Autowired
    OrderRepository orders;

    @Test
    void zipContainsExpectedStructure() throws Exception {
        Order o = orders.findById("seed-order-1").orElseThrow();

        var baos = new ByteArrayOutputStream();
        zipExporter.export(o.getId(), baos);

        Set<String> entries = new HashSet<>();
        try (var zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                entries.add(e.getName());
            }
        }

        assertEquals(14, entries.size(), "expected 14 entries, got: " + entries);

        String prefix = "order-seed-order-1/";
        assertTrue(entries.contains(prefix + "metadata.json"));
        assertTrue(entries.contains(prefix + "story.json"));
        assertTrue(entries.contains(prefix + "drawing.png"));
        assertTrue(entries.contains(prefix + "style.json"));
        for (int n = 1; n <= 5; n++) {
            String pn = String.format("%02d", n);
            assertTrue(entries.contains(prefix + "pages/page-" + pn + ".json"),
                "missing pages/page-" + pn + ".json");
            assertTrue(entries.contains(prefix + "pages/page-" + pn + ".png"),
                "missing pages/page-" + pn + ".png");
        }
    }

    @Test
    void metadataStatusHistoryIsStructuredArray() throws Exception {
        var baos = new ByteArrayOutputStream();
        zipExporter.export("seed-order-1", baos);

        byte[] metadataBytes = readEntry(baos.toByteArray(), "metadata.json");
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(metadataBytes);
        JsonNode history = root.get("statusHistory");

        assertTrue(history.isArray(),
            "statusHistory must be a JSON array, got: " + history.getNodeType());
        assertEquals(2, history.size(), "expected 2 history entries (PENDING + PROCESSING)");
        assertEquals("PENDING", history.get(0).get("status").asText());
        assertEquals("PROCESSING", history.get(1).get("status").asText());
        assertTrue(history.get(0).has("ts"));
    }

    private byte[] readEntry(byte[] zipBytes, String suffix) throws Exception {
        try (var zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if (e.getName().endsWith(suffix)) {
                    return zis.readAllBytes();
                }
            }
        }
        fail("entry not found: " + suffix);
        return new byte[0];
    }
}
