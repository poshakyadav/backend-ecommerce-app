package com.ecommerce.bkd.productSpecification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ecommerce.bkd.dtos.products.ProductSearchCriteria;
import com.ecommerce.bkd.models.Product;
import com.ecommerce.bkd.models.ProductVariant;
import com.ecommerce.bkd.models.SizePrice;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ProductSpecification implements Specification<ProductVariant> {

    private final ProductSearchCriteria criteria;

    public ProductSpecification(ProductSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<ProductVariant> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        Join<ProductVariant, Product> productJoin = root.join("product", JoinType.INNER);

        // Always join with sizePrices and filter for default
        Join<ProductVariant, SizePrice> sizePriceJoin = root.join("sizePrices", JoinType.INNER);
        predicates.add(cb.isTrue(sizePriceJoin.get("isDefault")));
        
        if (criteria.getQ() != null && !criteria.getQ().trim().isEmpty()) {
            String searchTerm = "%" + criteria.getQ().toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("name")), searchTerm),
                cb.like(cb.lower(root.get("description")), searchTerm),
                cb.like(cb.lower(productJoin.get("brand")), searchTerm),
                cb.like(cb.lower(productJoin.get("category").get("name")), searchTerm)
            ));
        }

        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            predicates.add(productJoin.get("category").get("name").in(criteria.getCategories()));
        }
        
        if (criteria.getMinRating() != null) {
            predicates.add(cb.greaterThanOrEqualTo(productJoin.get("averageRating"), criteria.getMinRating()));
        }

        // Apply price range filters using the existing join
        if (criteria.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(sizePriceJoin.get("totalPrice"), criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(sizePriceJoin.get("totalPrice"), criteria.getMaxPrice()));
        }

        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.isTrue(productJoin.get("active")));

        switch (criteria.getTab()) {
            case "new-arrivals":
                predicates.add(cb.isTrue(root.get("isNewArrival")));
                break;
            case "top-deals":
                predicates.add(cb.greaterThan(sizePriceJoin.get("discountAmount"), BigDecimal.ZERO));
                break;
            case "trendy":
                predicates.add(cb.greaterThan(root.get("viewCount"), 52L)); // Example threshold
                break;
            case "popular":
                predicates.add(cb.greaterThan(root.get("salesCount"), 50L)); // Example threshold
                break;
            case "recommended":
                predicates.add(cb.isTrue(root.get("isRecommended")));
                break;
            case "all":
            default:
                break;
        }
        

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}