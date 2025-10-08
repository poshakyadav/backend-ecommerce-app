package com.ecommerce.bkd.services;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.ecommerce.bkd.dtos.ProductImageDTO;
import com.ecommerce.bkd.dtos.products.ProductCreateRequest;
import com.ecommerce.bkd.dtos.products.ProductResponse;
import com.ecommerce.bkd.models.Product;
import com.ecommerce.bkd.models.ProductImage;

@Mapper(componentModel = "spring"
/* This for to suppress all warnings */
// , unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {
    @org.mapstruct.Mapping(source = "categoryId", target = "category.id")
    Product toEntity(ProductCreateRequest req); // This correct map a/q to need

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProductCreateRequest dto, @MappingTarget Product entity); // This correct map a/q to need

    ProductResponse toDto(Product product);

    ProductImageDTO imageToDto(ProductImage image);
}
