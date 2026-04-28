package com.sweetbook.repository;

import com.sweetbook.domain.story.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, String> {
    Optional<Page> findByStoryIdAndPageNumber(String storyId, int pageNumber);
}
