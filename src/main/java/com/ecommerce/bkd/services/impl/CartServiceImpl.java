package com.ecommerce.bkd.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.exceptions.CartException;
import com.ecommerce.bkd.models.Cart;
import com.ecommerce.bkd.models.CartItem;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.SizePrice;
import com.ecommerce.bkd.models.User;
import com.ecommerce.bkd.records.cart.CartDTO;
import com.ecommerce.bkd.records.cart.CartItemRequestDTO;
import com.ecommerce.bkd.records.products.ProductDetailDTO;
import com.ecommerce.bkd.repositories.CartItemRepository;
import com.ecommerce.bkd.repositories.CartRepository;
import com.ecommerce.bkd.repositories.ProductVariantRepository;
import com.ecommerce.bkd.repositories.UserRepository;
import com.ecommerce.bkd.services.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    private static final int MAX_CART_ITEMS = 100;
    private static final int MAX_QUANTITY_PER_ITEM = 10;

    @Override
    @Transactional
    public CartDTO addItemToCart(CartItemRequestDTO request, Long userId, String sessionId) {
        validateRequest(request);
        ProductVariant variant = variantRepository.findById(request.variantId())
            .orElseThrow(() -> new CartException("Variant not found"));
        if (!variant.isActive()) {
            throw new CartException("Variant inactive: " + variant.getSku());
        }

        SizePrice sizePrice = variant.getSizePrices().stream()
            .filter(sp -> sp.getSize().equals(request.selectedSize()))
            .findFirst()
            .orElseThrow(() -> new CartException("Size not found: " + request.selectedSize()));
        if (sizePrice.getStock() < request.quantity()) {
            throw new CartException("Insufficient stock for size " + request.selectedSize() + ": " + variant.getSku());
        }
        if (request.quantity() > MAX_QUANTITY_PER_ITEM) {
            throw new CartException("Quantity exceeds maximum limit per item: " + MAX_QUANTITY_PER_ITEM);
        }

        Cart cart = findOrCreateCart(userId, sessionId);
        if (cart.getItems().size() >= MAX_CART_ITEMS) {
            throw new CartException("Cart is full (max " + MAX_CART_ITEMS + " items)");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndVariantIdAndSelectedSize(
            cart.getId(), request.variantId(), request.selectedSize());
        CartItem item;
        if (existingItem.isPresent()) {
            item = existingItem.get();
            int newQuantity = item.getQuantity() + request.quantity();
            if (newQuantity > sizePrice.getStock() || newQuantity > MAX_QUANTITY_PER_ITEM) {
                throw new CartException("Requested quantity exceeds available stock or limit for size " + request.selectedSize());
            }
            item.setQuantity(newQuantity);
            syncItemPricing(item, sizePrice);
            item.calculatePricing(); // Manually ensure pricing is updated
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setVariant(variant);
            item.setQuantity(request.quantity());
            item.setSelectedSize(request.selectedSize());
            syncItemPricing(item, sizePrice);
            cart.getItems().add(item);
        }

        cartRepository.save(cart);
        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO updateItemQuantity(Long cartItemId, Integer quantity, Long userId) {
        if (quantity == null || quantity <= 0) {
            throw new CartException("Quantity must be greater than 0");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new CartException("Cart item not found with ID: " + cartItemId));
        validateCartOwnership(item.getCart(), userId);

        ProductVariant variant = item.getVariant();
        SizePrice sizePrice = variant.getSizePrices().stream()
            .filter(sp -> sp.getSize().equals(item.getSelectedSize()))
            .findFirst()
            .orElseThrow(() -> new CartException("Size not found: " + item.getSelectedSize()));

        if (quantity > sizePrice.getStock() || quantity > MAX_QUANTITY_PER_ITEM) {
            throw new CartException("Requested quantity exceeds available stock (" + sizePrice.getStock() + 
                ") or limit (" + MAX_QUANTITY_PER_ITEM + ") for size " + item.getSelectedSize());
        }

        item.setQuantity(quantity);
        syncItemPricing(item, sizePrice); // Ensure pricing is updated
        item.calculatePricing(); // Manually ensure pricing is updated
        cartItemRepository.save(item);
        cartItemRepository.flush(); // Force flush to ensure changes are persisted


        Cart cart = cartRepository.findById(item.getCart().getId())
            .orElseThrow(() -> new CartException("Cart not found"));
        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO removeItemFromCart(Long cartItemId, Long userId) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new CartException("Cart item not found"));
        validateCartOwnership(item.getCart(), userId);

        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO getCart(Long userId, String sessionId) {
        Cart cart = findOrCreateCart(userId, sessionId);
        cart.getItems().forEach(item -> {
            SizePrice sizePrice = item.getVariant().getSizePrices().stream()
                .filter(sp -> sp.getSize().equals(item.getSelectedSize()))
                .findFirst()
                .orElse(null);
            if (sizePrice != null) {
                syncItemPricing(item, sizePrice);
            }
        });
        cartRepository.save(cart);
        return mapToCartDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO mergeGuestCart(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            throw new CartException("User ID and session ID required");
        }
        validateSessionId(sessionId);

        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);
        if (guestCartOpt.isEmpty()) {
            return getCart(userId, null);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CartException("User not found"));
        Cart guestCart = guestCartOpt.get();
        Cart userCart = findOrCreateCart(userId, null);

        for (CartItem guestItem : guestCart.getItems()) {
            SizePrice sizePrice = guestItem.getVariant().getSizePrices().stream()
                .filter(sp -> sp.getSize().equals(guestItem.getSelectedSize()))
                .findFirst()
                .orElseThrow(() -> new CartException("Size not found: " + guestItem.getSelectedSize()));
            Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndVariantIdAndSelectedSize(
                userCart.getId(), guestItem.getVariant().getId(), guestItem.getSelectedSize());
            if (existingItem.isPresent()) {
                CartItem userItem = existingItem.get();
                int newQuantity = userItem.getQuantity() + guestItem.getQuantity();
                if (newQuantity > sizePrice.getStock() || newQuantity > MAX_QUANTITY_PER_ITEM) {
                    throw new CartException("Merged quantity exceeds stock or limit for size " + guestItem.getSelectedSize() + ": " + guestItem.getVariant().getSku());
                }
                userItem.setQuantity(newQuantity);
                syncItemPricing(userItem, sizePrice);
                userItem.calculatePricing(); // Ensure pricing is updated
                cartItemRepository.save(userItem);
            } else {
                if (userCart.getItems().size() >= MAX_CART_ITEMS) {
                    throw new CartException("Cart is full (max " + MAX_CART_ITEMS + " items)");
                }
                CartItem newItem = new CartItem();
                newItem.setCart(userCart);
                newItem.setVariant(guestItem.getVariant());
                newItem.setQuantity(guestItem.getQuantity());
                newItem.setSelectedSize(guestItem.getSelectedSize());
                syncItemPricing(newItem, sizePrice);
                userCart.getItems().add(newItem);
            }
        }

        cartRepository.delete(guestCart);
        cartRepository.save(userCart);
        return mapToCartDTO(userCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        if (userId == null) {
            throw new CartException("User ID required");
        }
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new CartException("Cart not found for user"));
        cart.getItems().clear();
        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.save(cart);
    }

    private Cart findOrCreateCart(Long userId, String sessionId) {
        Optional<Cart> cartOpt;
        User user = null;
    
        // First, try to find a cart by userId if provided
        if (userId != null) {
            user = userRepository.findById(userId)
                .orElseThrow(() -> new CartException("User not found"));
            cartOpt = cartRepository.findByUserId(userId);
            if (cartOpt.isPresent()) {
                return cartOpt.get(); // Return existing cart for user
            }
        }
    
        // If no userId or no cart for userId, try sessionId
        if (sessionId != null) {
            validateSessionId(sessionId);
            cartOpt = cartRepository.findBySessionId(sessionId);
            if (cartOpt.isPresent()) {
                // If a session-based cart exists, associate it with the user if userId is present
                Cart cart = cartOpt.get();
                if (user != null && cart.getUser() == null) {
                    cart.setUser(user);
                    cartRepository.save(cart); // Update cart with user
                }
                return cart;
            }
        }
    
        // If no cart exists, create a new one
        if (userId == null && sessionId == null) {
            throw new CartException("User ID or session ID required");
        }
    
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setSessionId(sessionId);
        cart.setItems(new ArrayList<>());
        return cartRepository.save(cart);
    }

    private void syncItemPricing(CartItem item, SizePrice sizePrice) {
        item.setBasePrice(sizePrice.getBasePrice());
        item.setDiscount(sizePrice.getDiscount() != null ? sizePrice.getDiscount() : BigDecimal.ZERO);
        item.setTaxRate(sizePrice.getTaxRate() != null ? sizePrice.getTaxRate() : BigDecimal.ZERO);
    }

    private void validateRequest(CartItemRequestDTO request) {
        if (request.variantId() == null || request.quantity() <= 0 || request.selectedSize() == null) {
            throw new CartException("Invalid variant ID, quantity, or size");
        }
    }

    private void validateCartOwnership(Cart cart, Long userId) {
        if (userId != null && (cart.getUser() == null || !cart.getUser().getId().equals(userId))) {
            throw new CartException("Unauthorized access to cart");
        }
    }

    private void validateSessionId(String sessionId) {
        if (sessionId == null || !sessionId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            throw new CartException("Invalid session ID format");
        }
    }

    private CartDTO mapToCartDTO(Cart cart) {
        List<CartDTO.CartItemDTO> itemDTOs = cart.getItems().stream()
            .map(item -> {
                ProductVariant variant = item.getVariant();
                SizePrice sizePrice = variant.getSizePrices().stream()
                    .filter(sp -> sp.getSize().equals(item.getSelectedSize()))
                    .findFirst()
                    .orElseThrow(() -> new CartException("Size not found: " + item.getSelectedSize()));
                BigDecimal basePrice = item.getBasePrice() != null ? item.getBasePrice() : BigDecimal.ZERO;
                BigDecimal discount = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;
                BigDecimal discountAmount = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
                BigDecimal discountedPrice = item.getDiscountedPrice() != null ? item.getDiscountedPrice() : BigDecimal.ZERO;
                BigDecimal taxRate = item.getTaxRate() != null ? item.getTaxRate() : BigDecimal.ZERO;
                BigDecimal taxAmount = item.getTaxAmount() != null ? item.getTaxAmount() : BigDecimal.ZERO;
                BigDecimal totalPrice = item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO;
                BigDecimal finalPrice = item.getFinalPrice() != null ? item.getFinalPrice() : BigDecimal.ZERO;
    
                return new CartDTO.CartItemDTO(
                    item.getId(),
                    new CartDTO.CartItemDTO.ProductVariantDTO(
                        variant.getId(),
                        variant.getSku(),
                        variant.getName(),
                        variant.getImageUrls(),
                        new ProductDetailDTO.ProductCategoryDTO(
                            variant.getProduct().getCategory().getId(),
                            variant.getProduct().getCategory().getName()
                        ),
                        Map.of("Color", variant.getColor().getName())
                    ),
                    item.getQuantity(),
                    item.getSelectedSize(),
                    basePrice,
                    discount,
                    discountAmount,
                    discountedPrice,
                    taxRate,
                    taxAmount,
                    totalPrice,
                    finalPrice
                );
            })
            .toList();
    
        // Corrected totalBasePrice calculation
        BigDecimal totalBasePrice = itemDTOs.stream()
            .map(item -> item.getBasePrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal totalDiscountAmount = itemDTOs.stream()
            .map(CartDTO.CartItemDTO::getDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal totalTaxAmount = itemDTOs.stream()
            .map(CartDTO.CartItemDTO::getTaxAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPrice = itemDTOs.stream()
            .map(CartDTO.CartItemDTO::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal totalFinalPrice = itemDTOs.stream()
            .map(CartDTO.CartItemDTO::getFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        return new CartDTO(
            cart.getId(),
            itemDTOs,
            totalBasePrice,
            totalDiscountAmount,
            totalTaxAmount,
            totalPrice,
            totalFinalPrice
        );
    }
}