package com.ecommerce.bkd.dtos.shiprocket;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnOrderItem {

    @JsonProperty("name")
    private String name;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("units")
    private Integer units;

    @JsonProperty("selling_price")
    private Integer sellingPrice;

    @JsonProperty("discount")
    private Integer discount;

    @JsonProperty("return_reason")
    private String returnReason;

    @JsonProperty("qc_enable")
    private Boolean qcEnable;

    @JsonProperty("qc_product_name")
    private String qcProductName;

    @JsonProperty("qc_product_image")
    private String qcProductImage;

    @JsonProperty("qc_brand")
    private String qcBrand;

    @JsonProperty("qc_color")
    private String qcColor;

    @JsonProperty("qc_size")
    private String qcSize;

    @JsonProperty("qc_serial_no")
    private String qcSerialNo;

    @JsonProperty("qc_ean_barcode")
    private String qcEanBarcode;

    @JsonProperty("qc_product_imei")
    private String qcProductImei;

    @JsonProperty("qc_brand_tag")
    private Boolean qcBrandTag;

    @JsonProperty("qc_used_check")
    private Boolean qcUsedCheck;

    @JsonProperty("qc_sealtag_check")
    private Boolean qcSealtagCheck;

    @JsonProperty("qc_check_damaged_product")
    private String qcCheckDamagedProduct;
}