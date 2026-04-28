package com.sweetbook.service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class SeedService {

    private final FileStorageService storage;

    public SeedService(FileStorageService storage) {
        this.storage = storage;
    }

    @Bean
    ApplicationRunner copySeedAssets() {
        return args -> {
            Path seedRoot = storage.resolve("seed");
            if (Files.exists(seedRoot.resolve("placeholder.png"))) {
                return;
            }

            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:seed/**/*.png");
            for (Resource r : resources) {
                String url = r.getURL().toString();
                int idx = url.indexOf("seed/");
                if (idx < 0) {
                    continue;
                }
                String relative = url.substring(idx);
                Path target = storage.getRoot().resolve(relative);
                Files.createDirectories(target.getParent());
                try (var in = r.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        };
    }
}
