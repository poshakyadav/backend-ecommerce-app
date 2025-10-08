package com.ecommerce.bkd.models.enums;

public enum OrderReturnStatus {

    NONE,               // default (no return requested)
     
    RETURN_REQUESTED, // Customer requested a return
    RETURN_APPROVED, // Return request approved
    RETURN_APPROVED_FAILED,
    RETURN_REJECTED, // Return request rejected

    RETURN_INITIATED, // Return process initiated by the seller
    
    RETURN_AWB_PENDING,
    RETURN_AWB_GENERATED, // AWB generated for return shipment

    RETURN_IN_TRANSIT,
   
    RETURNED, // Item has been returned
    
}
