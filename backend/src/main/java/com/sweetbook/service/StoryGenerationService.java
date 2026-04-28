package com.sweetbook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbook.domain.story.Page;
import com.sweetbook.domain.story.Story;
import com.sweetbook.domain.story.StoryStatus;
import com.sweetbook.repository.PageRepository;
import com.sweetbook.repository.StoryRepository;
import com.sweetbook.service.ai.AiClient;
import com.sweetbook.service.ai.StoryDraft;
import com.sweetbook.service.ai.StyleDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;

@Service
public class StoryGenerationService {

    // Note on @Transactional helpers below: generate() calls them on `this`, so Spring's
    // proxy is bypassed and the @Transactional annotation is *not* applied. The methods
    // still work because each repository.save() opens its own implicit transaction.
    // Treat the annotations as documentation of intent, not as load-bearing.

    private static final Logger log = LoggerFactory.getLogger(StoryGenerationService.class);

    private final StoryRepository stories;
    private final PageRepository pages;
    private final FileStorageService storage;
    private final AiClient ai;
    private final Executor storyExecutor;
    private final ObjectMapper om = new ObjectMapper();

    public StoryGenerationService(StoryRepository stories,
                                  PageRepository pages,
                                  FileStorageService storage,
                                  AiClient ai,
                                  @Qualifier("storyExecutor") Executor storyExecutor) {
        this.stories = stories;
        this.pages = pages;
        this.storage = storage;
        this.ai = ai;
        this.storyExecutor = storyExecutor;
    }

    @Async("storyExecutor")
    public void generate(String storyId) {
        try {
            Story s = loadOrThrow(storyId);
            byte[] drawingBytes = Files.readAllBytes(storage.resolve(s.getDrawingUrl()));
            String contentType = s.getDrawingUrl().endsWith(".png") ? "image/png" : "image/jpeg";

            transitionTo(storyId, StoryStatus.ANALYZING_DRAWING);
            StyleDescriptor style = ai.analyzeDrawing(drawingBytes, contentType);
            saveStyle(storyId, style);

            transitionTo(storyId, StoryStatus.GENERATING_STORY);
            StoryDraft draft = ai.generateStory(s.getChildName(), s.getImaginationPrompt(), style);
            saveDraft(storyId, draft);

            transitionTo(storyId, StoryStatus.GENERATING_IMAGES);
            for (int n = 1; n <= 5; n++) {
                generateIllustrationForPage(storyId, n, style);
            }

            transitionTo(storyId, StoryStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Story generation failed for {}", storyId, e);
            markFailed(storyId, "동화 생성 중 오류가 발생했어요: " + e.getMessage());
        }
    }

    @Transactional
    public void generateIllustrationForPage(String storyId, int pageNumber, StyleDescriptor style) {
        Page page = pages.findByStoryIdAndPageNumber(storyId, pageNumber)
            .orElseThrow(() -> new NoSuchElementException("PAGE_NOT_FOUND"));
        try {
            byte[] bytes = ai.generateIllustration(page.getIllustrationPrompt(), style);
            String path = storage.saveIllustration(storyId, pageNumber, bytes);
            page.setIllustrationUrl(path);
            pages.save(page);
        } catch (Exception e) {
            log.warn("Illustration failed for {} page {}: {}", storyId, pageNumber, e.getMessage());
        }
    }

    @Transactional
    public void regeneratePage(String storyId, int pageNumber) {
        Story s = loadOrThrow(storyId);
        StyleDescriptor style;
        try {
            style = om.readValue(s.getStyleDescriptorJson(), StyleDescriptor.class);
        } catch (Exception e) {
            style = new StyleDescriptor(List.of());
        }
        generateIllustrationForPage(storyId, pageNumber, style);
    }

    @Async("storyExecutor")
    public void retry(String storyId) {
        Story s = loadOrThrow(storyId);
        if (s.getStatus() != StoryStatus.FAILED) {
            log.warn("retry({}) ignored: story is in state {}, only FAILED can retry", storyId, s.getStatus());
            return;
        }
        Story fresh = loadOrThrow(storyId);
        fresh.retry();
        stories.save(fresh);
        storyExecutor.execute(() -> generate(storyId));
    }

    @Transactional
    public void transitionTo(String storyId, StoryStatus target) {
        Story s = loadOrThrow(storyId);
        s.transitionTo(target);
        stories.save(s);
    }

    @Transactional
    public void markFailed(String storyId, String message) {
        Story s = loadOrThrow(storyId);
        if (!s.getStatus().isTerminal()) {
            s.markFailed(message);
            stories.save(s);
        }
    }

    @Transactional
    public void saveStyle(String storyId, StyleDescriptor style) {
        try {
            Story s = loadOrThrow(storyId);
            s.setStyleDescriptorJson(om.writeValueAsString(style));
            stories.save(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void saveDraft(String storyId, StoryDraft draft) {
        Story s = loadOrThrow(storyId);
        s.setTitle(draft.title());
        for (StoryDraft.PageDraft pd : draft.pages()) {
            Page page = pages.findByStoryIdAndPageNumber(storyId, pd.pageNumber())
                .orElseGet(() -> Page.create(s, pd.pageNumber()));
            page.setBodyText(pd.bodyText());
            page.setIllustrationPrompt(pd.illustrationPrompt());
            pages.save(page);
        }
        stories.save(s);
    }

    private Story loadOrThrow(String id) {
        return stories.findById(id).orElseThrow(() -> new NoSuchElementException("STORY_NOT_FOUND"));
    }
}
