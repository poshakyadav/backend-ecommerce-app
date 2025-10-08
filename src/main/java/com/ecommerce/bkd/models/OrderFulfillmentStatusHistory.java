package com.ecommerce.bkd.models;

import java.time.LocalDateTime;

import com.ecommerce.bkd.models.enums.OrderFulfillmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class OrderFulfillmentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Which order this history row belongs to.
     * Many history entries → one order.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * The status that was set on the order at this moment.
     * Reuses the same enum you already have.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderFulfillmentStatus status;

    private String note;

    /**
     * Exactly when we changed to this status.
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public OrderFulfillmentStatusHistory(Order order, OrderFulfillmentStatus status, String note,
            LocalDateTime changedAt) {
        this.order = order;
        this.status = status;
        this.note = note;
        this.changedAt = changedAt;
    }

    

}
