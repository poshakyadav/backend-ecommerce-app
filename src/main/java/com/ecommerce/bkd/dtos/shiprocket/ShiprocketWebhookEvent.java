package com.ecommerce.bkd.dtos.shiprocket;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the JSON body sent by Shiprocket on tracking/webhook events.
 */
@Data
public class ShiprocketWebhookEvent {

    /** The airway bill number **/
    @JsonProperty("awb")
    private String awb;

    /** Courier name, e.g. "Delhivery Surface" **/
    @JsonProperty("courier_name")
    private String courierName;

    /** The human‐readable status, e.g. "IN TRANSIT" **/
    @JsonProperty("current_status")
    private String currentStatus;

    
    /** Numeric code for current status **/
    @JsonProperty("current_status_id")
    private Integer currentStatusId;

    /** Overall shipment status, e.g. "IN TRANSIT" **/
    @JsonProperty("shipment_status")
    private String shipmentStatus;

    @JsonProperty("shipment_status_id")
    private Integer shipmentStatusId;

    /** Timestamp of this event, e.g. "23 05 2023 11:43:52" **/
    @JsonProperty("current_timestamp")
    private String currentTimestamp;

    // external ID used when order was created
    /** Your own order identifier (the one you passed when creating the order) **/
    @JsonProperty("order_id")
    private String orderId;

    // internal Shiprocket order index
    /** Shiprocket’s internal numeric order ID **/
    @JsonProperty("sr_order_id")
    private Long srOrderId;

    @JsonProperty("awb_assigned_date")
    private String awbAssignedDate;       // date AWB was assigned

    @JsonProperty( "pickup_scheduled_date")
    private String pickupScheduledDate;   // scheduled pickup time

    /** Estimated time of delivery **/
    @JsonProperty("etd")
    private String etd;

    /** A list of scan events (if you need them) **/
    private List<ShiprocketScan> scans;

    /** 0 or 1 indicating if this is a return event **/
    @JsonProperty("is_return")
    private boolean isReturn;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("pod_status")
    private String podStatus;

    @JsonProperty("pod")
    private String pod;

    
}
