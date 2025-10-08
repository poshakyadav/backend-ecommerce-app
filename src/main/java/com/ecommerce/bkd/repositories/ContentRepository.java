package com.ecommerce.bkd.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.bkd.models.Content;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByContentKey(String contentKey);
}