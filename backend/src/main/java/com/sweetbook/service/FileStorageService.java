package com.sweetbook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(root.resolve("drawings"));
            Files.createDirectories(root.resolve("illustrations"));
            Files.createDirectories(root.resolve("seed"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String saveDrawing(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("UNSUPPORTED_IMAGE_TYPE");
        }
        return saveDrawingBytes(file.getBytes(), contentType);
    }

    public String saveDrawingBytes(byte[] bytes, String contentType) throws IOException {
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("UNSUPPORTED_IMAGE_TYPE");
        }
        String ext = contentType.equals("image/png") ? ".png" : ".jpg";
        String name = UUID.randomUUID() + ext;
        String rel = "drawings/" + name;
        Path target = root.resolve(rel);
        Files.write(target, bytes);
        return rel;
    }

    public String saveIllustration(String storyId, int pageNumber, byte[] bytes) throws IOException {
        String rel = String.format("illustrations/%s/page-%02d.png", storyId, pageNumber);
        Path target = root.resolve(rel);
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
        return rel;
    }

    public Path resolve(String relativePath) {
        return root.resolve(relativePath).toAbsolutePath();
    }

    public Path getRoot() {
        return root;
    }
}
