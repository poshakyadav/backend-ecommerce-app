package com.ecommerce.bkd.services;

import com.ecommerce.bkd.dtos.shiprocket.ReturnOrderRequest;
import com.ecommerce.bkd.models.ReturnOrder;
import com.ecommerce.bkd.records.returnOrder.ReturnOrderDTO;

public interface ReturnOrderMapper {

    ReturnOrderDTO toReturnOrderDTO(ReturnOrder ro);

    ReturnOrderRequest toShiprocketReturnOrderRequest(ReturnOrder ro);

}
