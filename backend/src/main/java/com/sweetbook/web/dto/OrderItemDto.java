package com.sweetbook.web.dto;

import com.sweetbook.domain.order.BookSize;
import com.sweetbook.domain.order.CoverType;

public record OrderItemDto(BookSize bookSize, CoverType coverType, int copies) {}
