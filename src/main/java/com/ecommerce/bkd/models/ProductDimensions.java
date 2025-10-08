package com.ecommerce.bkd.models;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ProductDimensions {
    private Double length;
    private Double width;
    private Double height;
    private Double weight;
}
