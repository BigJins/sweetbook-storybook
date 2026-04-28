package com.sweetbook.service;

import com.sweetbook.domain.order.Order;
import com.sweetbook.domain.order.OrderItem;
import com.sweetbook.domain.order.OrderStatus;
import com.sweetbook.domain.story.Story;
import com.sweetbook.domain.story.StoryStatus;
import com.sweetbook.repository.OrderRepository;
import com.sweetbook.repository.StoryRepository;
import com.sweetbook.web.dto.OrderCreateRequest;
import com.sweetbook.web.dto.OrderDto;
import com.sweetbook.web.dto.OrderItemDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orders;
    private final StoryRepository stories;

    public OrderService(OrderRepository orders, StoryRepository stories) {
        this.orders = orders;
        this.stories = stories;
    }

    @Transactional
    public OrderDto create(OrderCreateRequest req) {
        Story s = stories.findById(req.storyId())
            .orElseThrow(() -> new NoSuchElementException("STORY_NOT_FOUND"));
        if (s.getStatus() != StoryStatus.COMPLETED) {
            throw new IllegalArgumentException("STORY_NOT_COMPLETED");
        }
        Order o = Order.create(s, req.recipientName(), req.addressMemo());
        OrderItem item = OrderItem.create(o, req.bookSize(), req.coverType(), req.copies());
        o.setItem(item);
        Order saved = orders.save(o);
        return toDto(saved);
    }

    public List<OrderDto> list() {
        return orders.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public OrderDto updateStatus(String id, OrderStatus target) {
        Order o = orders.findById(id)
            .orElseThrow(() -> new NoSuchElementException("ORDER_NOT_FOUND"));
        o.transitionTo(target);
        return toDto(orders.save(o));
    }

    public Order getEntity(String id) {
        return orders.findById(id)
            .orElseThrow(() -> new NoSuchElementException("ORDER_NOT_FOUND"));
    }

    private OrderDto toDto(Order o) {
        Story s = o.getStory();
        String coverUrl = s.getPages().stream()
            .filter(p -> p.getPageNumber() == 1)
            .findFirst()
            .map(p -> StoryService.toFileUrl(p.getIllustrationUrl()))
            .orElse(null);
        OrderItem item = o.getItem();
        OrderItemDto itemDto = item == null ? null
            : new OrderItemDto(item.getBookSize(), item.getCoverType(), item.getCopies());
        return new OrderDto(
            o.getId(),
            new OrderDto.OrderStorySummary(s.getId(), s.getTitle(), coverUrl),
            o.getStatus(),
            o.getRecipientName(),
            o.getAddressMemo(),
            itemDto,
            o.getCreatedAt()
        );
    }
}
