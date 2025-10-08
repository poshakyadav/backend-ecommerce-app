package com.ecommerce.bkd.dtos.products;

import java.util.List;

import lombok.Data;

@Data
public class VariantImageUpdateDTO {
    private List<String> imageUrlsToKeep;
    private List<String> imageOrder;
}