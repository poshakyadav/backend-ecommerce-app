package com.ecommerce.bkd.dtos.shiprocket;

import lombok.Data;

@Data
public class ShipmentListRequest {
    private String sort; // ASC or DESC
    private String sortBy; // Field to sort by (e.g., "id", "created_at")
    private String filter; // Filter value
    private String filterBy; // Field to filter by (e.g., "status", "awb")
}
