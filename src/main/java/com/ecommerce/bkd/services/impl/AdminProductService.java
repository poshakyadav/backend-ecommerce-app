package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.bkd.dtos.products.AdminProductListDTO;
import com.ecommerce.bkd.dtos.products.ProductBasicUpdateDTO;
import com.ecommerce.bkd.dtos.products.ProductUpdateDTO;
import com.ecommerce.bkd.dtos.products.VariantColorDTO;
import com.ecommerce.bkd.dtos.products.VariantImageUpdateDTO;
import com.ecommerce.bkd.dtos.products.VariantUpdateDTO;
import com.ecommerce.bkd.models.Product;
import com.ecommerce.bkd.models.ProductCategory;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.SizePrice;
import com.ecommerce.bkd.models.VariantColor;
import com.ecommerce.bkd.records.products.ProductCreateDTO;
import com.ecommerce.bkd.records.products.ProductDetailDTO;
import com.ecommerce.bkd.repositories.OrderItemRepository;
import com.ecommerce.bkd.repositories.ProductCategoryRepository;
import com.ecommerce.bkd.repositories.ProductRepository;
import com.ecommerce.bkd.repositories.ProductVariantRepository;
import com.ecommerce.bkd.repositories.VariantColorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductCategoryRepository categoryRepository;
    private final VariantColorRepository variantColorRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductServiceImpl productService;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<AdminProductListDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToAdminProductListDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDetailDTO getProductByVariantId(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        return productService.getProductDetails(variant.getProduct().getId(), variantId);
    }

    @Transactional
    public void toggleProductActive(Long productId, boolean active) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setActive(active);
        product.getVariants().forEach(v -> v.setActive(active));
        productRepository.save(product);
    }

    @Transactional
    public void toggleVariantActive(Long variantId, boolean active) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        variant.setActive(active);
        variantRepository.save(variant);
    }

    @Transactional
    public ProductDetailDTO createProduct(ProductCreateDTO dto, Map<Integer, List<MultipartFile>> variantImages) {
        ProductCategory category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Product product = new Product();
        product.setBrand(dto.brand());
        product.setCategory(category);
        product.setActive(true);

        List<ProductVariant> variants = new ArrayList<>();
        for (int i = 0; i < dto.variants().size(); i++) {
            ProductCreateDTO.VariantDTO variantDTO = dto.variants().get(i);
            validateVariantDTO(variantDTO);
            if (variantDTO.sizePrices().isEmpty()) {
                throw new IllegalArgumentException("Each variant must have at least one size price");
            }
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setName(variantDTO.name());
            variant.setSku(variantDTO.sku());
            variant.setDescription(variantDTO.description());
            variant.setLength(variantDTO.length());
            variant.setBreadth(variantDTO.breadth());
            variant.setHeight(variantDTO.height());
            variant.setWeight(variantDTO.weight());
            variant.setActive(true);
            variant.setNewArrival(variantDTO.isNewArrival());
            variant.setTrendy(variantDTO.isTrendy());
            variant.setRecommended(variantDTO.isRecommended());
            variant.setViewCount(0L);
            variant.setSalesCount(0L);

            VariantColor color = variantColorRepository.findByNameAndHexCode(
                    variantDTO.color().getName(), variantDTO.color().getHexCode()
            ).orElseGet(() -> {
                VariantColor newColor = new VariantColor();
                newColor.setName(variantDTO.color().getName());
                newColor.setHexCode(variantDTO.color().getHexCode());
                return variantColorRepository.save(newColor);
            });
            variant.setColor(color);

            List<SizePrice> sizePrices = new ArrayList<>();
            boolean hasDefault = false;
            for (ProductCreateDTO.VariantDTO.SizePriceDTO sizeDTO : variantDTO.sizePrices()) {
                SizePrice sizePrice = new SizePrice();
                sizePrice.setVariant(variant);
                sizePrice.setSize(sizeDTO.size());
                sizePrice.setBasePrice(sizeDTO.basePrice());
                sizePrice.setDiscount(sizeDTO.discount() != null ? sizeDTO.discount() : BigDecimal.ZERO);
                sizePrice.setTaxRate(sizeDTO.taxRate() != null ? sizeDTO.taxRate() : BigDecimal.ZERO);
                sizePrice.setStock(sizeDTO.stock());
                sizePrice.setDefault(sizeDTO.isDefault());
                if (sizeDTO.isDefault()) {
                    if (hasDefault) {
                        throw new IllegalArgumentException("Only one size can be default per variant");
                    }
                    hasDefault = true;
                }
                sizePrices.add(sizePrice);
            }
            if (!hasDefault && !sizePrices.isEmpty()) {
                sizePrices.get(0).setDefault(true);
            }
            variant.setSizePrices(sizePrices);

            // Upload images for this variant
            List<MultipartFile> images = variantImages.getOrDefault(i, List.of());
            if (!images.isEmpty()) {
                Map<String, List<String>> uploadResult = cloudinaryService.uploadMultipleImages(images, "products/variant_" + variant.getSku());
                variant.setImageUrls(uploadResult.get("urls"));
                variant.setImagePublicIds(uploadResult.get("publicIds"));
            } else {
                log.warn("No images provided for variant {} with SKU {}", i, variantDTO.sku());
            }

            variants.add(variant);
        }

        product.setVariants(variants);
        productRepository.save(product);
        log.info("Product created with ID {}", product.getId());
        return productService.getProductDetails(product.getId(), null);
    }

    @Transactional
    public ProductDetailDTO updateProduct(Long productId, ProductUpdateDTO dto, Map<Integer, List<MultipartFile>> newVariantImages) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.setBrand(dto.brand());
        product.setCategory(categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found")));

        if (dto.newVariants() != null && !dto.newVariants().isEmpty()) {
            for (int i = 0; i < dto.newVariants().size(); i++) {
                ProductUpdateDTO.NewVariantDTO variantDTO = dto.newVariants().get(i);
                if (variantRepository.existsBySku(variantDTO.sku())) {
                    throw new IllegalArgumentException("SKU " + variantDTO.sku() + " already exists");
                }
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setName(variantDTO.name());
                variant.setSku(variantDTO.sku());
                variant.setDescription(variantDTO.description());
                variant.setLength(variantDTO.length());
                variant.setBreadth(variantDTO.breadth());
                variant.setHeight(variantDTO.height());
                variant.setWeight(variantDTO.weight());
                variant.setActive(true);
                variant.setNewArrival(variantDTO.isNewArrival());
                variant.setTrendy(variantDTO.isTrendy());
                variant.setRecommended(variantDTO.isRecommended());
                // variant.setViewCount(0L);
                // variant.setSalesCount(0L);

                VariantColor color = variantColorRepository.findByNameAndHexCode(
                        variantDTO.color().name(), variantDTO.color().hexCode()
                ).orElseGet(() -> {
                    VariantColor newColor = new VariantColor();
                    newColor.setName(variantDTO.color().name());
                    newColor.setHexCode(variantDTO.color().hexCode());
                    return variantColorRepository.save(newColor);
                });
                variant.setColor(color);

                List<SizePrice> sizePrices = new ArrayList<>();
                boolean hasDefault = false;
                for (ProductUpdateDTO.NewVariantDTO.SizePriceDTO sizeDTO : variantDTO.sizePrices()) {
                    SizePrice sizePrice = new SizePrice();
                    sizePrice.setVariant(variant);
                    sizePrice.setSize(sizeDTO.size());
                    sizePrice.setBasePrice(sizeDTO.basePrice());
                    sizePrice.setDiscount(sizeDTO.discount() != null ? sizeDTO.discount() : BigDecimal.ZERO);
                    sizePrice.setTaxRate(sizeDTO.taxRate() != null ? sizeDTO.taxRate() : BigDecimal.ZERO);
                    sizePrice.setStock(sizeDTO.stock());
                    sizePrice.setDefault(sizeDTO.isDefault());
                    if (sizeDTO.isDefault()) {
                        if (hasDefault) {
                            throw new IllegalArgumentException("Only one size can be default per variant");
                        }
                        hasDefault = true;
                    }
                    sizePrices.add(sizePrice);
                }
                if (!hasDefault && !sizePrices.isEmpty()) {
                    sizePrices.get(0).setDefault(true);
                }
                variant.setSizePrices(sizePrices);

                List<MultipartFile> images = newVariantImages.getOrDefault(i, List.of());
                if (!images.isEmpty()) {
                    Map<String, List<String>> uploadResult = cloudinaryService.uploadMultipleImages(images, "products/variant_" + variant.getSku());
                    variant.setImageUrls(uploadResult.get("urls"));
                    variant.setImagePublicIds(uploadResult.get("publicIds"));
                }

                product.getVariants().add(variant);
            }
        }

        productRepository.save(product);
        log.info("Product {} updated successfully with new variants", productId);
        return productService.getProductDetails(product.getId(), null);
    }

    @Transactional
    public ProductDetailDTO updateProductBasic(ProductBasicUpdateDTO dto) {
        // Redirect to updateProduct with no new variants
        ProductUpdateDTO updateDTO = new ProductUpdateDTO(dto.getId(), dto.getBrand(), dto.getCategoryId(), List.of());
        return updateProduct(dto.getId(), updateDTO, Map.of());
    }

    @Transactional
    public ProductDetailDTO updateVariantDetails(Long variantId, VariantUpdateDTO dto) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        validateVariantDTO(dto);
        if (dto.getSizePrices().isEmpty()) {
            throw new IllegalArgumentException("Each variant must have at least one size price");
        }

        // Update variant basic details
        variant.setSku(dto.getSku());
        variant.setName(dto.getName());
        variant.setDescription(dto.getDescription());
        variant.setLength(dto.getLength());
        variant.setBreadth(dto.getBreadth());
        variant.setHeight(dto.getHeight());
        variant.setWeight(dto.getWeight());

        variant.setNewArrival(dto.isNewArrival());
        variant.setTrendy(dto.isTrendy());
        variant.setRecommended(dto.isRecommended());        

        // Update or set color
        VariantColor color = variantColorRepository.findByNameAndHexCode(
                dto.getColor().getName(), dto.getColor().getHexCode()
        ).orElseGet(() -> {
            VariantColor newColor = new VariantColor();
            newColor.setName(dto.getColor().getName());
            newColor.setHexCode(dto.getColor().getHexCode());
            return variantColorRepository.save(newColor);
        });
        variant.setColor(color);

        // Map existing SizePrice entries by size for efficient lookup
        Map<String, SizePrice> existingSizePrices = variant.getSizePrices().stream()
                .collect(Collectors.toMap(SizePrice::getSize, sp -> sp, (sp1, sp2) -> sp1));

        // Process DTO SizePrice entries
        List<SizePrice> updatedSizePrices = new ArrayList<>();
        boolean hasDefault = false;

        for (VariantUpdateDTO.SizePriceDTO sizeDTO : dto.getSizePrices()) {
            SizePrice sizePrice;
            String sizeKey = sizeDTO.getSize();

            if (existingSizePrices.containsKey(sizeKey)) {
                // Update existing SizePrice
                sizePrice = existingSizePrices.get(sizeKey);
                sizePrice.setBasePrice(sizeDTO.getBasePrice());
                sizePrice.setDiscount(sizeDTO.getDiscount() != null ? sizeDTO.getDiscount() : BigDecimal.ZERO);
                sizePrice.setTaxRate(sizeDTO.getTaxRate() != null ? sizeDTO.getTaxRate() : BigDecimal.ZERO);
                sizePrice.setStock(sizeDTO.getStock());
                sizePrice.setDefault(sizeDTO.isDefault());
            } else {
                // Create new SizePrice
                sizePrice = new SizePrice();
                sizePrice.setVariant(variant);
                sizePrice.setSize(sizeDTO.getSize());
                sizePrice.setBasePrice(sizeDTO.getBasePrice());
                sizePrice.setDiscount(sizeDTO.getDiscount() != null ? sizeDTO.getDiscount() : BigDecimal.ZERO);
                sizePrice.setTaxRate(sizeDTO.getTaxRate() != null ? sizeDTO.getTaxRate() : BigDecimal.ZERO);
                sizePrice.setStock(sizeDTO.getStock());
                sizePrice.setDefault(sizeDTO.isDefault());
                variant.getSizePrices().add(sizePrice);
            }

            if (sizeDTO.isDefault()) {
                if (hasDefault) {
                    throw new IllegalArgumentException("Only one size can be default per variant");
                }
                hasDefault = true;
            }
            updatedSizePrices.add(sizePrice);
        }

        // Ensure there’s a default size if none is set
        if (!hasDefault && !updatedSizePrices.isEmpty()) {
            updatedSizePrices.get(0).setDefault(true);
        }

        // Note: Existing SizePrice entries not in DTO remain unchanged (no removal)

        variantRepository.save(variant);
        log.info("Variant {} details updated successfully", variantId);
        return productService.getProductDetails(variant.getProduct().getId(), variantId);
    }

    @Transactional
    public ProductDetailDTO updateVariantImages(Long variantId, VariantImageUpdateDTO dto, List<MultipartFile> imageFiles) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        List<String> currentPublicIds = variant.getImagePublicIds();
        List<String> currentUrls = variant.getImageUrls();
        List<String> urlsToKeep = dto.getImageUrlsToKeep() != null ? dto.getImageUrlsToKeep() : List.of();
        List<String> orderedUrls = dto.getImageOrder() != null ? dto.getImageOrder() : urlsToKeep;

        if (orderedUrls.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 images allowed per variant");
        }

        List<String> publicIdsToKeep = orderedUrls.stream()
                .map(this::extractPublicId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> publicIdsToDelete = currentPublicIds.stream()
                .filter(id -> !publicIdsToKeep.contains(id))
                .collect(Collectors.toList());

        if (!publicIdsToDelete.isEmpty()) {
            cloudinaryService.deleteImages(publicIdsToDelete);
        }

        List<String> newImageUrls = new ArrayList<>();
        List<String> newPublicIds = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            if (orderedUrls.size() + imageFiles.size() > 5) {
                throw new IllegalArgumentException("Total images cannot exceed 5");
            }
            Map<String, List<String>> uploadResult = cloudinaryService.uploadMultipleImages(imageFiles, "products/variant_" + variant.getSku());
            newImageUrls = uploadResult.get("urls");
            newPublicIds = uploadResult.get("publicIds");
        }

        List<String> finalImageUrls = new ArrayList<>(orderedUrls);
        finalImageUrls.addAll(newImageUrls);
        List<String> finalPublicIds = new ArrayList<>(publicIdsToKeep);
        finalPublicIds.addAll(newPublicIds);

        variant.setImageUrls(finalImageUrls);
        variant.setImagePublicIds(finalPublicIds);

        variantRepository.save(variant);
        log.info("Variant {} images updated successfully", variantId);
        return productService.getProductDetails(variant.getProduct().getId(), variantId);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Check for existing orders
        boolean hasOrders = orderItemRepository.existsByVariantProductId(productId);
        if (hasOrders) {
            throw new IllegalStateException("Cannot delete product with existing orders");
        }

        // Delete associated images from Cloudinary (if applicable)
        for (ProductVariant variant : product.getVariants()) {
            if (!variant.getImageUrls().isEmpty()) {
                cloudinaryService.deleteImages(variant.getImageUrls());
            }
        }

        productRepository.delete(product);
    }

    @Transactional
    public void deleteVariant(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        // Check for existing orders
        boolean hasOrders = orderItemRepository.existsByVariantId(variantId);
        if (hasOrders) {
            throw new IllegalStateException("Cannot delete variant with existing orders");
        }

        // Delete associated images from Cloudinary
        if (!variant.getImageUrls().isEmpty()) {
            cloudinaryService.deleteImages(variant.getImageUrls());
        }

        Product product = variant.getProduct();
        variantRepository.delete(variant);

        // Update default variant if necessary
        if (product.getDefaultVariantId() != null && product.getDefaultVariantId().equals(variantId)) {
            List<ProductVariant> activeVariants = variantRepository.findByProductId(product.getId());
            product.setDefaultVariantId(activeVariants.isEmpty() ? null : activeVariants.get(0).getId());
            productRepository.save(product);
        }
    }

    private void validateVariantDTO(ProductCreateDTO.VariantDTO variantDTO) {
        if (variantDTO.name() == null || variantDTO.name().isBlank()) {
            throw new IllegalArgumentException("Variant name is required");
        }
        Set<String> sizes = variantDTO.sizePrices().stream()
                .map(ProductCreateDTO.VariantDTO.SizePriceDTO::size)
                .collect(Collectors.toSet());
        if (sizes.size() != variantDTO.sizePrices().size()) {
            throw new IllegalArgumentException("Duplicate sizes are not allowed in a variant");
        }
    }

    private void validateVariantDTO(VariantUpdateDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Variant name is required");
        }
        Set<String> sizes = dto.getSizePrices().stream()
                .map(VariantUpdateDTO.SizePriceDTO::getSize)
                .collect(Collectors.toSet());
        if (sizes.size() != dto.getSizePrices().size()) {
            throw new IllegalArgumentException("Duplicate sizes are not allowed in a variant");
        }
    }

    private String extractPublicId(String url) {
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) return null;
        String path = url.substring(uploadIndex + "/upload/".length());
        if (path.startsWith("v")) {
            int slashIndex = path.indexOf("/");
            if (slashIndex != -1) {
                path = path.substring(slashIndex + 1);
            }
        }
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex != -1) {
            path = path.substring(0, dotIndex);
        }
        return path;
    }

    private AdminProductListDTO mapToAdminProductListDTO(Product product) {
        AdminProductListDTO dto = new AdminProductListDTO();
        dto.setId(product.getId());
        dto.setBrand(product.getBrand());
        dto.setActive(product.isActive());
        dto.setCategory(new AdminProductListDTO.ProductCategoryDTO(
                product.getCategory().getId(),
                product.getCategory().getName()
        ));
        dto.setVariants(product.getVariants().stream().map(v -> {
            AdminProductListDTO.VariantDTO variantDTO = new AdminProductListDTO.VariantDTO();
            variantDTO.setId(v.getId());
            variantDTO.setName(v.getName());
            variantDTO.setSku(v.getSku());
            variantDTO.setDescription(v.getDescription());
            variantDTO.setImageUrls(v.getImageUrls());
            variantDTO.setActive(v.isActive());
            variantDTO.setLength(v.getLength());
            variantDTO.setBreadth(v.getBreadth());
            variantDTO.setHeight(v.getHeight());
            variantDTO.setWeight(v.getWeight());
            variantDTO.setColor(new VariantColorDTO(
                    v.getColor().getId(),
                    v.getColor().getName(),
                    v.getColor().getHexCode()
            ));

            variantDTO.setNewArrival(v.isNewArrival());
            variantDTO.setTrendy(v.isTrendy());
            variantDTO.setRecommended(v.isRecommended());
            variantDTO.setViewCount(v.getViewCount());
            variantDTO.setSalesCount(v.getSalesCount());

            variantDTO.setSizePrices(v.getSizePrices().stream()
                    .map(sp -> new AdminProductListDTO.SizePriceDTO(
                            sp.getId(),
                            sp.getSize(),
                            sp.getBasePrice(),
                            sp.getDiscount(),
                            sp.getDiscountAmount(),
                            sp.getDiscountedPrice(),
                            sp.getTaxRate(),
                            sp.getTaxAmount(),
                            sp.getTotalPrice(),
                            sp.getFinalPrice(),
                            sp.getStock(),
                            sp.isDefault()
                    )).collect(Collectors.toList()));
            return variantDTO;
        }).collect(Collectors.toList()));
        return dto;
    }
}