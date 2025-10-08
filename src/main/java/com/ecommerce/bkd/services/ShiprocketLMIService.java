package com.ecommerce.bkd.services;

import java.util.List;

import com.ecommerce.bkd.dtos.shiprocket.InvoiceResponse;
import com.ecommerce.bkd.dtos.shiprocket.LabelResponse;
import com.ecommerce.bkd.dtos.shiprocket.ManifestResponse;
import com.ecommerce.bkd.dtos.shiprocket.PrintManifestResponse;

public interface ShiprocketLMIService {

    LabelResponse generateLabel(List<Long> shipmentIds);

    ManifestResponse generateManifest(List<Long> shipmentIds);

    InvoiceResponse generateInvoice(List<String> orderIds);

    PrintManifestResponse printManifest(List<Long> orderIds);

}
