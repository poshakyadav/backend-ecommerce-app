package com.ecommerce.bkd.dtos.shiprocket;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OrderListRequest {
    private Integer page;
    private Integer perPage;
    private String filter;
    private String filterBy;
    private String sort; // ASC or DESC
    private String sortBy; // Field to sort by
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;
    private String awbNumber;
}
