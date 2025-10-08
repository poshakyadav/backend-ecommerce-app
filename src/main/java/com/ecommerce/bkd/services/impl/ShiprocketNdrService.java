package com.ecommerce.bkd.services.impl;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecommerce.bkd.dtos.shiprocket.ActionNdrResponse;
import com.ecommerce.bkd.dtos.shiprocket.NdrActionRequest;
import com.ecommerce.bkd.dtos.shiprocket.NdrFilter;
import com.ecommerce.bkd.dtos.shiprocket.NdrShipment;
import com.ecommerce.bkd.dtos.shiprocket.ShiprocketPagedResponse;
import com.ecommerce.bkd.exceptions.NdrIntegrationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketNdrService {
    private final ShiprocketApiService apiService;

    /**
     * GET /ndr/all - list all NDR shipments with optional filters
     */
    public ShiprocketPagedResponse<NdrShipment> listAllNdr(NdrFilter filter) {
        String uri = UriComponentsBuilder.fromPath("/ndr/all")
                .queryParam("page", filter.getPage())
                .queryParam("per_page", filter.getPerPage())
                .queryParamIfPresent("from", filter.getFromDate())
                .queryParamIfPresent("to", filter.getToDate())
                .queryParamIfPresent("search", filter.getSearchTerm())
                .build()
                .toUriString();

        try {
            return apiService.get(uri,
                    new ParameterizedTypeReference<ShiprocketPagedResponse<NdrShipment>>() {
                    });
        } catch (Exception e) {
            log.error("Failed to list NDR shipments: {}", filter, e);
            throw new NdrIntegrationException("Could not fetch NDR shipments", e);
        }
    }

    /**
     * Convenience overload for pagination-only calls
     */
    public ShiprocketPagedResponse<NdrShipment> listAllNdr(int page, int perPage) {
        return listAllNdr(NdrFilter.builder()
                .page(page)
                .perPage(perPage)
                .build());
    }

    /**
     * GET /ndr/{awbCode} - get specific NDR details (may return paged list)
     */
    public ShiprocketPagedResponse<NdrShipment> getNdrDetails(String awbCode) {
        String path = "/ndr/" + awbCode;
        try {
            return apiService.get(path,
                    new ParameterizedTypeReference<ShiprocketPagedResponse<NdrShipment>>() {
                    });
        } catch (Exception e) {
            log.error("Failed to fetch NDR details for AWB={}", awbCode, e);
            throw new NdrIntegrationException("Could not fetch NDR details", e);
        }
    }

    /**
     * POST /ndr/{awbCode}/action - take action on NDR (e.g. return, reattempt,
     * update address)
     */
    public ActionNdrResponse actionNdr(String awbCode, NdrActionRequest request) {
        String path = "/ndr/" + awbCode + "/action";
        try {
            return apiService.post(path, request, ActionNdrResponse.class);
        } catch (Exception e) {
            log.error("Failed to perform NDR action {} for AWB={}", request, awbCode, e);
            throw new NdrIntegrationException("Could not perform NDR action", e);
        }
    }
}