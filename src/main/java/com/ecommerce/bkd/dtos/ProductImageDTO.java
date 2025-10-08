package com.ecommerce.bkd.dtos;

import lombok.Data;

@Data
public class ProductImageDTO {
    private Long id;
    private String imageUrl;
    private boolean isMainImage;
}
