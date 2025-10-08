package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.dtos.wishlist.WishlistDTO;
import com.ecommerce.bkd.dtos.wishlist.WishlistItemRequestDTO;
import com.ecommerce.bkd.exceptions.WishlistException;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.SizePrice;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.models.Wishlist;
import com.ecommerce.bkd.models.WishlistItem;
import com.ecommerce.bkd.records.cart.CartDTO;
import com.ecommerce.bkd.records.cart.CartItemRequestDTO;
import com.ecommerce.bkd.records.products.ProductDetailDTO;
import com.ecommerce.bkd.repositories.ProductVariantRepository;
import com.ecommerce.bkd.repositories.UserRepository;
import com.ecommerce.bkd.repositories.WishlistItemRepository;
import com.ecommerce.bkd.repositories.WishlistRepository;
import com.ecommerce.bkd.services.WishlistService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final CartServiceImpl cartService;

    private static final int MAX_WISHLIST_ITEMS = 200;

    @Override
    @Transactional
    public WishlistDTO addItemToWishlist(WishlistItemRequestDTO request, Long userId) {
        validateUser(userId);
        ProductVariant variant = variantRepository.findById(request.getVariantId())
            .orElseThrow(() -> new WishlistException("Variant not found"));
        if (!variant.isActive()) {
            throw new WishlistException("Variant is inactive");
        }

        Wishlist wishlist = findOrCreateWishlist(userId);
        if (wishlist.getItems().size() >= MAX_WISHLIST_ITEMS) {
            throw new WishlistException("Wishlist is full (max " + MAX_WISHLIST_ITEMS + " items)");
        }

        Optional<WishlistItem> existingItem = wishlistItemRepository.findByWishlistIdAndVariantId(
            wishlist.getId(), request.getVariantId());
        if (existingItem.isPresent()) {
            throw new WishlistException("Item already in wishlist");
        }

        WishlistItem item = new WishlistItem();
        item.setWishlist(wishlist);
        item.setVariant(variant);
        wishlist.getItems().add(item);
        wishlistRepository.save(wishlist);
        return mapToWishlistDTO(wishlist);
    }

    @Override
    @Transactional
    public WishlistDTO removeItemFromWishlist(Long wishlistItemId, Long userId) {
        validateUser(userId);
        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
            .orElseThrow(() -> new WishlistException("Wishlist item not found"));
        if (!item.getWishlist().getUser().getId().equals(userId)) {
            throw new WishlistException("Unauthorized access to wishlist item");
        }

        Wishlist wishlist = item.getWishlist();
        wishlist.getItems().remove(item);
        wishlistItemRepository.delete(item);
        wishlistRepository.save(wishlist);
        return mapToWishlistDTO(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistDTO getWishlist(Long userId) {
        validateUser(userId);
        Wishlist wishlist = findOrCreateWishlist(userId);
        return mapToWishlistDTO(wishlist);
    }

    @Override
    @Transactional
    public WishlistDTO moveItemToCart(Long wishlistItemId, Long userId, String selectedSize) {
        validateUser(userId);
        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
            .orElseThrow(() -> new WishlistException("Wishlist item not found"));
        if (!item.getWishlist().getUser().getId().equals(userId)) {
            throw new WishlistException("Unauthorized access to wishlist item");
        }

        if (selectedSize == null) {
            throw new WishlistException("Size selection required to move item to cart");
        }

        CartDTO cartDTO = addWishlistItemToCart(item, userId, selectedSize);
        Wishlist wishlist = item.getWishlist();
        wishlist.getItems().remove(item);
        wishlistItemRepository.delete(item);
        wishlistRepository.save(wishlist);
        return mapToWishlistDTO(wishlist);
    }

    @Override
    @Transactional
    public WishlistDTO moveAllItemsToCart(Long userId, Map<Long, String> sizeSelections) {
        validateUser(userId);
        Wishlist wishlist = findOrCreateWishlist(userId);
        for (WishlistItem item : List.copyOf(wishlist.getItems())) {
            String selectedSize = sizeSelections.get(item.getId());
            if (selectedSize == null) {
                continue; // Skip items without size selection
            }
            addWishlistItemToCart(item, userId, selectedSize);
            wishlist.getItems().remove(item);
            wishlistItemRepository.delete(item);
        }
        wishlistRepository.save(wishlist);
        return mapToWishlistDTO(wishlist);
    }

    @Override
    @Transactional
    public void clearWishlist(Long userId) {
        validateUser(userId);
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
            .orElseThrow(() -> new WishlistException("Wishlist not found"));
        wishlist.getItems().clear();
        wishlistItemRepository.deleteByWishlistId(wishlist.getId());
        wishlistRepository.save(wishlist);
    }

    private Wishlist findOrCreateWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new WishlistException("User not found"));
                Wishlist wishlist = new Wishlist();
                wishlist.setUser(user);
                wishlist.setItems(new ArrayList<>());
                return wishlistRepository.save(wishlist);
            });
    }

    private CartDTO addWishlistItemToCart(WishlistItem item, Long userId , String selectedSize ) {
        ProductVariant variant = item.getVariant();
        SizePrice sizePrice = variant.getSizePrices().stream()
            .filter(sp -> sp.getSize().equals(selectedSize))
            .findFirst()
            .orElseThrow(() -> new WishlistException("Size not found: " + selectedSize));

        if (!variant.isActive() || sizePrice.getStock() == 0) {
            throw new WishlistException("Variant is inactive or size out of stock: " + variant.getSku());
        }
        CartItemRequestDTO cartRequest = new CartItemRequestDTO(variant.getId(), 1, selectedSize); // Default quantity
        return cartService.addItemToCart(cartRequest, userId, null);
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            throw new WishlistException("User authentication required");
        }
        userRepository.findById(userId)
            .orElseThrow(() -> new WishlistException("User not found"));
    }

    private WishlistDTO mapToWishlistDTO(Wishlist wishlist) {
        List<WishlistDTO.WishlistItemDTO> itemDTOs = wishlist.getItems().stream()
            .map(item -> {
                ProductVariant variant = item.getVariant();
                SizePrice defaultSizePrice = variant.getSizePrices().stream()
                    .filter(SizePrice::isDefault)
                    .findFirst()
                    .orElse(variant.getSizePrices().get(0));
                boolean inStock = variant.isActive() && defaultSizePrice.getStock() > 0;
                boolean priceChanged = !defaultSizePrice.getDiscountedPrice().equals(
                    defaultSizePrice.getBasePrice().subtract(
                        defaultSizePrice.getDiscount() != null ?
                            defaultSizePrice.getBasePrice().multiply(defaultSizePrice.getDiscount()) : BigDecimal.ZERO));
                return new WishlistDTO.WishlistItemDTO(
                    item.getId(),
                    new WishlistDTO.WishlistItemDTO.ProductVariantDTO(
                        variant.getId(),
                        variant.getSku(),
                        variant.getHsnCode(),
                        variant.getName(),
                        variant.getProduct().getBrand(),
                        variant.getDescription(),
                        variant.getImageUrls(),
                        new ProductDetailDTO.ProductCategoryDTO(
                            variant.getProduct().getCategory().getId(),
                            variant.getProduct().getCategory().getName()
                        ),
                        Map.of("Color", variant.getColor().getName())
                    ),
                    item.getAddedAt(),
                    inStock,
                    priceChanged,
                    defaultSizePrice.getDiscountedPrice()
                );
            })
            .collect(Collectors.toList());

        return new WishlistDTO(wishlist.getId(), itemDTOs);
    }
}