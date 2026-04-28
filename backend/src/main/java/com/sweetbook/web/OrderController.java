package com.sweetbook.web;

import com.sweetbook.domain.order.OrderStatus;
import com.sweetbook.service.OrderService;
import com.sweetbook.web.dto.OrderCreateRequest;
import com.sweetbook.web.dto.OrderDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public OrderDto create(@Valid @RequestBody OrderCreateRequest req) {
        return orders.create(req);
    }

    @GetMapping
    public List<OrderDto> list() {
        return orders.list();
    }

    @PatchMapping("/{id}/status")
    public OrderDto updateStatus(@PathVariable String id,
                                 @RequestBody Map<String, String> body) {
        OrderStatus target = OrderStatus.valueOf(body.get("status"));
        return orders.updateStatus(id, target);
    }
}
