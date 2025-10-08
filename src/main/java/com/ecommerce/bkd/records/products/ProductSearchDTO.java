package com.ecommerce.bkd.records.products;

import java.util.List;

public record ProductSearchDTO(
    Long productId,
    ProductDetailDTO.ProductCategoryDTO category,
    List<ProductVariantDTO> variants
) {}