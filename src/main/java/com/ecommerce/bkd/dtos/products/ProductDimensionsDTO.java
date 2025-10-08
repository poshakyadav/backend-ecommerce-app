package com.ecommerce.bkd.dtos.products;

import java.io.Serializable;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDimensionsDTO implements Serializable {

    @Min(0)
    private Double weight;

    @Min(0)
    private Double length;

    @Min(0)
    private Double width;

    @Min(0)
    private Double height;
}