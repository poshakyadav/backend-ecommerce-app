package com.ecommerce.bkd.controllers.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.bkd.dtos.ApiResponse;
import com.ecommerce.bkd.models.Content;
import com.ecommerce.bkd.services.ContentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Content>>> getAllContent() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "All content retrieved successfully",
                contentService.getAllContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Content>> getContent(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Content retrieved successfully",
                contentService.getContent(id)));
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<ApiResponse<Content>> getContentByKey(@PathVariable String key) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Content retrieved successfully for key: " + key,
                contentService.getContentByKey(key)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Content>> createContent(
            @Valid @RequestBody Content content) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Content created successfully",
                contentService.createContent(content)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Content>> updateContent(
            @PathVariable Long id,
            @Valid @RequestBody Content content) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Content updated successfully",
                contentService.updateContent(id, content)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Content deleted successfully"));
    }
}