package com.sweetbook.service;

import com.sweetbook.domain.story.Page;
import com.sweetbook.domain.story.Story;
import com.sweetbook.repository.PageRepository;
import com.sweetbook.repository.StoryRepository;
import com.sweetbook.web.dto.PageDto;
import com.sweetbook.web.dto.StoryCreateRequest;
import com.sweetbook.web.dto.StoryDto;
import com.sweetbook.web.dto.StorySummaryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class StoryService {

    private final StoryRepository stories;
    private final PageRepository pages;
    private final FileStorageService storage;
    private final StoryGenerationService generationService;

    public StoryService(StoryRepository stories,
                        PageRepository pages,
                        FileStorageService storage,
                        StoryGenerationService generationService) {
        this.stories = stories;
        this.pages = pages;
        this.storage = storage;
        this.generationService = generationService;
    }

    @Transactional
    public Story create(StoryCreateRequest req, MultipartFile drawing) throws IOException {
        if (drawing == null || drawing.isEmpty()) {
            throw new IllegalArgumentException("DRAWING_REQUIRED");
        }
        String drawingPath = storage.saveDrawing(drawing);
        Story s = Story.newDraft(req.childName(), req.imaginationPrompt());
        s.setDrawingUrl(drawingPath);
        return stories.save(s);
    }

    public void kickOffAsyncGeneration(String storyId) {
        generationService.generate(storyId);
    }

    @Transactional
    public void updatePageBody(String storyId, int pageNumber, String bodyText) {
        Page p = pages.findByStoryIdAndPageNumber(storyId, pageNumber)
            .orElseThrow(() -> new NoSuchElementException("PAGE_NOT_FOUND"));
        p.setBodyText(bodyText);
        pages.save(p);
    }

    public List<StorySummaryDto> list() {
        return stories.findAllByOrderByCreatedAtDesc().stream()
            .map(s -> {
                String coverUrl = s.getPages().stream()
                    .filter(p -> p.getPageNumber() == 1)
                    .map(p -> toFileUrl(p.getIllustrationUrl()))
                    .findFirst()
                    .orElse(null);
                return new StorySummaryDto(
                    s.getId(),
                    s.getTitle(),
                    s.getChildName(),
                    s.getStatus(),
                    coverUrl,
                    s.getCreatedAt(),
                    s.getErrorMessage()
                );
            })
            .toList();
    }

    public StoryDto getById(String id) {
        Story s = stories.findById(id)
            .orElseThrow(() -> new NoSuchElementException("STORY_NOT_FOUND"));
        List<PageDto> pageDtos = s.getPages().stream()
            .map(p -> new PageDto(
                p.getPageNumber(),
                p.getLayout(),
                p.getBodyText(),
                p.getIllustrationPrompt(),
                toFileUrl(p.getIllustrationUrl())
            ))
            .toList();
        return new StoryDto(
            s.getId(),
            s.getTitle(),
            s.getChildName(),
            s.getStatus(),
            s.getErrorMessage(),
            toFileUrl(s.getDrawingUrl()),
            s.getStyleDescriptorJson(),
            s.getImaginationPrompt(),
            pageDtos,
            s.getCreatedAt()
        );
    }

    public static String toFileUrl(String relativePath) {
        return relativePath == null ? null : "/api/files/" + relativePath;
    }
}
