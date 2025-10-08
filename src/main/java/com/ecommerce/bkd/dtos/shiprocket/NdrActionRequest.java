package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NdrActionRequest {
    private String action;
    private String comments;
    private String phone;
    @JsonProperty("proof_audio")
    private String proofAudio;
    @JsonProperty("proof_image")
    private String proofImage;
    private String remarks;
    @JsonProperty("address1")
    private String address1;
    @JsonProperty("address2")
    private String address2;
    @JsonProperty("deferred_date")
    private String deferredDate;
}