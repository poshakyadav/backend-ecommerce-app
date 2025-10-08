package com.ecommerce.bkd.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDimensions {
    private Double length;
    private Double breadth;
    private Double height;
    private Double weight;
}
