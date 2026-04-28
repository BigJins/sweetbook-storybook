package com.sweetbook.service;

import com.sweetbook.domain.story.Story;
import com.sweetbook.repository.StoryRepository;
import com.sweetbook.web.dto.PageDto;
import com.sweetbook.web.dto.StoryDto;
import com.sweetbook.web.dto.StorySummaryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class StoryService {

    private final StoryRepository stories;

    public StoryService(StoryRepository stories) {
        this.stories = stories;
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
