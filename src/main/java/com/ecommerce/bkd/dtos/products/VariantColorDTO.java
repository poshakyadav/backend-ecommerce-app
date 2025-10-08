package com.ecommerce.bkd.dtos.products;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantColorDTO {
    private Long id;
    
    @NotBlank
    private String name; // e.g., "Crimson Red"
    
    @NotBlank
    @Pattern(regexp = "#[0-9A-Fa-f]{6}")
    private String hexCode; // e.g., "#DC143C"
}