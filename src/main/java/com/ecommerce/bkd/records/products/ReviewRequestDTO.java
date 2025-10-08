package com.ecommerce.bkd.records.products;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequestDTO {
    @NotNull
    private Long orderItemId;
    
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    @Size(max = 1000)
    private String reviewText;
}