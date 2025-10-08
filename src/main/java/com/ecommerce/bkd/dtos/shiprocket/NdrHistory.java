package com.ecommerce.bkd.dtos.shiprocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NdrHistory {
    private Long id;
    @JsonProperty("ndr_id")
    private Long ndrId;
    @JsonProperty("ndr_reason")
    private String ndrReason;
    @JsonProperty("action_by")
    private Integer actionBy;
    @JsonProperty("ndr_attempt")
    private Integer ndrAttempt;
    private String medium;
    @JsonProperty("ndr_push_status")
    private Integer ndrPushStatus;
    private String comment;
    @JsonProperty("call_center_call_recording")
    private String callCenterRecording;
    @JsonProperty("call_center_recording_date")
    private String callCenterRecordingDate;
    private String proofRecording;
    private String proofImage;
    @JsonProperty("sms_response")
    private String smsResponse;
    @JsonProperty("ndr_raised_at")
    private String ndrRaisedAt;
}