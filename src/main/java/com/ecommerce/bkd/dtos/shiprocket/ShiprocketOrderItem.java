package com.ecommerce.bkd.dtos.shiprocket;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiprocketOrderItem {

    @JsonProperty("name")
    private String name;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("units")
    private Integer units;

    @JsonProperty("selling_price")
    private BigDecimal sellingPrice;

    @JsonProperty("discount")
    private BigDecimal discount;

    @JsonProperty("tax")
    private BigDecimal tax;

    @JsonProperty("hsn")
    private String hsn;
}
