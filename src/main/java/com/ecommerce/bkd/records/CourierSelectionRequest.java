package com.ecommerce.bkd.records;

import jakarta.validation.constraints.NotNull;

public record CourierSelectionRequest(
    @NotNull Integer courierId
) { }