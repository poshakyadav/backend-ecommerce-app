package com.ecommerce.bkd.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.bkd.dtos.products.CloudinaryProductImageDTO;
import com.ecommerce.bkd.exceptions.ImageStorageException;
import com.ecommerce.bkd.exceptions.ImageValidationException;
import com.ecommerce.bkd.services.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    @Override
    public CloudinaryProductImageDTO uploadImage(MultipartFile file) {
        validateImage(file);
        try (InputStream in = file.getInputStream()) {
            Map<?, ?> res = cloudinary.uploader().upload(in, ObjectUtils.asMap(
                    "folder", "products",
                    "use_filename", true,
                    "unique_filename", false));

            // Extract both publicId and URL from the Cloudinary response
            String publicId = (String) res.get("public_id");
            String url = (String) res.get("secure_url");
            // persist publicId alongside your ProductImage entity
            return new CloudinaryProductImageDTO(url, publicId);
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new ImageStorageException("Failed to upload image", e);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            Map<?, ?> res = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            if (!"ok".equals(res.get("result"))) {
                log.warn("Cloudinary delete unexpected result: {}", res);
            }
        } catch (IOException e) {
            log.error("Cloudinary deletion failed for {}", publicId, e);
            throw new ImageStorageException("Failed to delete image", e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > 5_000_000L) {
            throw new ImageValidationException("File too large");
        }
        String type = file.getContentType();
        if (!List.of("image/jpeg", "image/png").contains(type)) {
            throw new ImageValidationException("Unsupported file type: " + type);
        }
    }
}
