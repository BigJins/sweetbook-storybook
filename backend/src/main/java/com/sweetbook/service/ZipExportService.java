package com.sweetbook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sweetbook.domain.order.Order;
import com.sweetbook.domain.story.Page;
import com.sweetbook.domain.story.Story;
import com.sweetbook.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipExportService {

    private final OrderRepository orders;
    private final FileStorageService storage;
    private final ObjectMapper om = new ObjectMapper();

    public ZipExportService(OrderRepository orders, FileStorageService storage) {
        this.orders = orders;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public void export(String orderId, OutputStream out) throws IOException {
        Order o = orders.findById(orderId)
            .orElseThrow(() -> new NoSuchElementException("ORDER_NOT_FOUND"));
        Story s = o.getStory();
        String prefix = "order-" + o.getId() + "/";

        try (var zos = new ZipOutputStream(out)) {
            putJson(zos, prefix + "metadata.json", buildMetadata(o));

            putJson(zos, prefix + "story.json", Map.of(
                "id", s.getId(),
                "title", s.getTitle(),
                "childName", s.getChildName(),
                "imaginationPrompt", s.getImaginationPrompt(),
                "pageCount", s.getPages().size()
            ));

            String styleRaw = s.getStyleDescriptorJson();
            byte[] styleBytes = (styleRaw == null ? "{}" : styleRaw).getBytes();
            putBytes(zos, prefix + "style.json", styleBytes);

            if (s.getDrawingUrl() != null) {
                putFile(zos, prefix + "drawing.png", storage.resolve(s.getDrawingUrl()));
            }

            for (Page p : s.getPages()) {
                String pn = String.format("%02d", p.getPageNumber());

                Map<String, Object> pageMeta = new LinkedHashMap<>();
                pageMeta.put("pageNumber", p.getPageNumber());
                pageMeta.put("layout", p.getLayout().name());
                pageMeta.put("bodyText", p.getBodyText());
                pageMeta.put("illustrationPrompt", p.getIllustrationPrompt());
                pageMeta.put("illustrationMissing", p.getIllustrationUrl() == null);
                putJson(zos, prefix + "pages/page-" + pn + ".json", pageMeta);

                Path src = p.getIllustrationUrl() != null
                    ? storage.resolve(p.getIllustrationUrl())
                    : storage.resolve("seed/placeholder.png");
                if (Files.exists(src)) {
                    putFile(zos, prefix + "pages/page-" + pn + ".png", src);
                }
            }
        }
    }

    private Map<String, Object> buildMetadata(Order o) {
        var item = o.getItem();
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("orderId", o.getId());
        meta.put("status", o.getStatus().name());
        meta.put("recipientName", o.getRecipientName());
        meta.put("addressMemo", o.getAddressMemo());
        if (item != null) {
            meta.put("bookSize", item.getBookSize().name());
            meta.put("coverType", item.getCoverType().name());
            meta.put("copies", item.getCopies());
        }
        meta.put("createdAt", o.getCreatedAt().toString());
        meta.put("statusHistory", parseStatusHistory(o.getStatusHistoryJson()));
        return meta;
    }

    private JsonNode parseStatusHistory(String raw) {
        if (raw == null || raw.isBlank()) {
            return om.createArrayNode();
        }
        try {
            JsonNode node = om.readTree(raw);
            // H2's JSON column stores values as JSON strings (e.g. "[...]")
            // while MySQL stores them as native JSON. Unwrap one level if needed.
            if (node.isTextual()) {
                node = om.readTree(node.asText());
            }
            return node.isArray() ? node : (ArrayNode) om.createArrayNode().add(node);
        } catch (IOException e) {
            return om.createArrayNode();
        }
    }

    private void putJson(ZipOutputStream zos, String name, Object data) throws IOException {
        byte[] bytes = om.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
        putBytes(zos, name, bytes);
    }

    private void putBytes(ZipOutputStream zos, String name, byte[] bytes) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(bytes);
        zos.closeEntry();
    }

    private void putFile(ZipOutputStream zos, String name, Path src) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        Files.copy(src, zos);
        zos.closeEntry();
    }
}
