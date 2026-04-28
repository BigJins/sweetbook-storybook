package com.sweetbook.domain.story;

public enum PageLayout {
    COVER,
    SPLIT,
    ENDING;

    public static PageLayout forPageNumber(int pageNumber) {
        if (pageNumber == 1) {
            return COVER;
        }
        if (pageNumber == 5) {
            return ENDING;
        }
        return SPLIT;
    }
}
