package com.ecommerce.bkd.dtos.shiprocket;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AwbData {

    @JsonProperty("courier_company_id")
    private Integer courierCompanyId;

    @JsonProperty("awb_code")
    private String awbCode;

    /** 0 or 1, whether this shipment is COD. */
    @JsonProperty("cod")
    private Integer cod;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("shipment_id")
    private Long shipmentId;

    @JsonProperty("awb_code_status")
    private Integer awbCodeStatus;

    /**
     * This object holds date + timezone info.
     */
    @JsonProperty("assigned_date_time")
    private AssignedDateTime assignedDateTime;

    /** The weight actually applied to this AWB. */
    @JsonProperty("applied_weight")
    private BigDecimal appliedWeight;

    @JsonProperty("company_id")
    private Integer companyId;

    @JsonProperty("courier_name")
    private String courierName;

    @JsonProperty("child_courier_name")
    private String childCourierName;

    @JsonProperty("pickup_scheduled_date")
    private String pickupScheduledDate;

    @JsonProperty("routing_code")
    private String routingCode;

    @JsonProperty("rto_routing_code")
    private String rtoRoutingCode;

    @JsonProperty("invoice_no")
    private String invoiceNo;

    @JsonProperty("transporter_id")
    private String transporterId;

    @JsonProperty("transporter_name")
    private String transporterName;

    /**
     * The “shipped_by” block is a nested object with shipper/RTO info.
     */
    @JsonProperty("shipped_by")
    private ShippedBy shippedBy;
}