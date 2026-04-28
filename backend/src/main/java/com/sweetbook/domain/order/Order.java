package com.sweetbook.domain.order;

import com.sweetbook.domain.story.Story;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "recipient_name", nullable = false, length = 30)
    private String recipientName;

    @Column(name = "address_memo", columnDefinition = "TEXT")
    private String addressMemo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status;

    @Column(name = "status_history", nullable = false, columnDefinition = "json")
    private String statusHistoryJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Order() {
    }

    public static Order create(Story story, String recipientName, String addressMemo) {
        Order order = new Order();
        Instant now = Instant.now();
        order.id = UUID.randomUUID().toString();
        order.story = story;
        order.recipientName = recipientName;
        order.addressMemo = addressMemo;
        order.status = OrderStatus.PENDING;
        order.statusHistoryJson = "[]";
        order.createdAt = now;
        order.updatedAt = now;
        return order;
    }

    public void transitionTo(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid order transition: " + status + " -> " + target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public Story getStory() {
        return story;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
