package com.ecommerce.bkd.dtos.shiprocket;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Channel {
    private Long id;
    private String name;
    private String channelCode;
    private String status;
    private LocalDateTime createdAt;
}
