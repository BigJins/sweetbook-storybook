package com.sweetbook.domain.story;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "story")
public class Story {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "child_name", nullable = false, length = 20)
    private String childName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StoryStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "drawing_url", length = 255)
    private String drawingUrl;

    @Column(name = "style_descriptor", columnDefinition = "json")
    private String styleDescriptorJson;

    @Column(name = "imagination_prompt", nullable = false, columnDefinition = "TEXT")
    private String imaginationPrompt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Story() {
    }

    public static Story newDraft(String childName, String imaginationPrompt) {
        Story story = new Story();
        Instant now = Instant.now();
        story.id = UUID.randomUUID().toString();
        story.title = "";
        story.childName = childName;
        story.status = StoryStatus.DRAFT;
        story.imaginationPrompt = imaginationPrompt;
        story.createdAt = now;
        story.updatedAt = now;
        return story;
    }

    public void transitionTo(StoryStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid story transition: " + status + " -> " + target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getChildName() {
        return childName;
    }

    public StoryStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getDrawingUrl() {
        return drawingUrl;
    }

    public String getStyleDescriptorJson() {
        return styleDescriptorJson;
    }

    public String getImaginationPrompt() {
        return imaginationPrompt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
