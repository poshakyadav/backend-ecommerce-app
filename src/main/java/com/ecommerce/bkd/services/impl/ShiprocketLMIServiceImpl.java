package com.ecommerce.bkd.services.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ecommerce.bkd.dtos.shiprocket.InvoiceResponse;
import com.ecommerce.bkd.dtos.shiprocket.LabelResponse;
import com.ecommerce.bkd.dtos.shiprocket.ManifestResponse;
import com.ecommerce.bkd.dtos.shiprocket.PrintManifestResponse;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.services.ShiprocketLMIService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for generating labels, manifests, and invoices using the Shiprocket
 * API.
 * This service handles the creation of PDF URLs for labels and manifests,
 * as well as printing invoices and manifests for shipments.
 * LMI stands for Label, Manifest, and Invoice.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketLMIServiceImpl implements ShiprocketLMIService {

    private final ShiprocketApiService apiService;

    /**
     * Generate label PDF URLs for given shipment IDs
     */
    public LabelResponse generateLabel(List<Long> shipmentIds) {
        try {
            return apiService.post("/courier/generate/label",
                    Map.of("shipment_id", shipmentIds), LabelResponse.class);
        } catch (Exception e) {
            log.error("Error generating label: ", e);
            throw new ShiprocketApiException("Failed to generate label: " + e.getMessage());
        }
    }

    /**
     * Generate a manifest (PDF URL) for given shipment IDs
     */
    public ManifestResponse generateManifest(List<Long> shipmentIds) {
        try {
            return apiService.post("/manifests/generate",
                    Map.of("shipment_id", shipmentIds), ManifestResponse.class);
        } catch (Exception e) {
            log.error("Error generating manifest: ", e);
            throw new ShiprocketApiException("Failed to generate manifest: " + e.getMessage());
        }
    }

    /**
     * Print invoice for given order IDs
     */
    public InvoiceResponse generateInvoice(List<String> orderIds) {
        try {
            return apiService.post("/orders/print/invoice",
                    Map.of("ids", orderIds),
                    InvoiceResponse.class);
        } catch (Exception e) {
            log.error("Error generating invoice: ", e);
            throw new ShiprocketApiException("Failed to generate invoice: " + e.getMessage());
        }
    }

    /**
     * Print manifest → returns the URL in the response payload
     */
    public PrintManifestResponse printManifest(List<Long> orderIds) {
        try {
            return apiService.post(
                    "/manifests/print",
                    Map.of("order_ids", orderIds),
                    PrintManifestResponse.class);
        } catch (Exception e) {
            log.error("Error printing manifest: ", e);
            throw new ShiprocketApiException("Failed to print manifest: " + e.getMessage());
        }
    }

}
