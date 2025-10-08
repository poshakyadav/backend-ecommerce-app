package com.ecommerce.bkd.dtos.shiprocket;

import java.util.List;

import lombok.Data;

@Data
public class ShiprocketPagedResponse<T> {
    private List<T> data;
    private PaginationMeta meta;
}