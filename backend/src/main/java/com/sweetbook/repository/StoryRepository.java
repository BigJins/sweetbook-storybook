package com.sweetbook.repository;

import com.sweetbook.domain.story.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, String> {
    List<Story> findAllByOrderByCreatedAtDesc();
}
