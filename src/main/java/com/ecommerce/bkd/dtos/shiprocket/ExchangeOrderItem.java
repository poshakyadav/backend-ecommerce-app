package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ExchangeOrderItem {
    private String name;
    private String selling_price;
    private String units;
    private String hsn;
    private String sku;
    private String tax;
    private String discount;
    private String brand;
    private String color;

    @JsonProperty("exchange_item_id")
    private String exchangeItemId;
    @JsonProperty("exchange_item_name")
    private String exchangeItemName;
    @JsonProperty("exchange_item_sku")
    private String exchangeItemSku;

    private boolean qc_enable;
    private String qc_product_name;
    private String qc_product_image;
    private String qc_brand;
    private String qc_color;
    private String qc_size;
    private String accessories;
    private String qc_used_check;
    private String qc_sealtag_check;
    private String qc_brand_box;
    private String qc_check_damaged_product;
}
