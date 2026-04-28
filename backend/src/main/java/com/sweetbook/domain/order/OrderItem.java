package com.sweetbook.domain.order;

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
@Table(name = "order_item")
public class OrderItem {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_size", nullable = false, length = 8)
    private BookSize bookSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "cover_type", nullable = false, length = 8)
    private CoverType coverType;

    @Column(nullable = false)
    private int copies;

    protected OrderItem() {
    }

    public static OrderItem create(Order order, BookSize bookSize, CoverType coverType, int copies) {
        OrderItem item = new OrderItem();
        item.id = UUID.randomUUID().toString();
        item.order = order;
        item.bookSize = bookSize;
        item.coverType = coverType;
        item.copies = copies;
        return item;
    }
}
