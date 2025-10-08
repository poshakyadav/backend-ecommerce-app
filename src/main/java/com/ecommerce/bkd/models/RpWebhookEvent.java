package com.ecommerce.bkd.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rp_webhook_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RpWebhookEvent {

    @Id
    @Column(name = "event_id", length = 100)
    private String eventId; // X-Razorpay-Event-Id

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

}
