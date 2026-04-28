package com.sweetbook.domain.story;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "page")
public class Page {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PageLayout layout;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "illustration_prompt", columnDefinition = "TEXT")
    private String illustrationPrompt;

    @Column(name = "illustration_url", length = 255)
    private String illustrationUrl;

    protected Page() {
    }

    public static Page create(Story story, int pageNumber) {
        Page page = new Page();
        page.id = UUID.randomUUID().toString();
        page.story = story;
        page.pageNumber = pageNumber;
        page.layout = PageLayout.forPageNumber(pageNumber);
        return page;
    }

    public String getId() {
        return id;
    }

    public Story getStory() {
        return story;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public PageLayout getLayout() {
        return layout;
    }
}
