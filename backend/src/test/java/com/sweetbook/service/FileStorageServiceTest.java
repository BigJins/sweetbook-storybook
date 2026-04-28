package com.sweetbook.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    @TempDir
    Path tmp;

    FileStorageService svc;

    @BeforeEach
    void setUp() {
        svc = new FileStorageService(tmp.toString());
    }

    @Test
    void saveDrawingReturnsRelativePath() throws Exception {
        var file = new MockMultipartFile("drawing", "kid.png", "image/png", "PNG_BYTES".getBytes());
        String path = svc.saveDrawing(file);
        assertTrue(path.startsWith("drawings/"), "got: " + path);
        assertTrue(path.endsWith(".png"));
        assertTrue(Files.exists(tmp.resolve(path)));
    }

    @Test
    void saveIllustrationByStoryAndPage() throws Exception {
        byte[] bytes = "PNG".getBytes();
        String path = svc.saveIllustration("story-abc", 3, bytes);
        assertEquals("illustrations/story-abc/page-03.png", path);
        assertArrayEquals(bytes, Files.readAllBytes(tmp.resolve(path)));
    }

    @Test
    void resolveAbsolutePath() {
        Path p = svc.resolve("seed/story-1/cover.png");
        assertEquals(tmp.resolve("seed/story-1/cover.png").toAbsolutePath(), p.toAbsolutePath());
    }

    @Test
    void rejectsInvalidContentType() {
        var file = new MockMultipartFile("drawing", "x.gif", "image/gif", "GIF".getBytes());
        assertThrows(IllegalArgumentException.class, () -> svc.saveDrawing(file));
    }
}
