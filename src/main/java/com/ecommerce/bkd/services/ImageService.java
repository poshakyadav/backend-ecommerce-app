package com.ecommerce.bkd.services;

import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.bkd.dtos.products.CloudinaryProductImageDTO;

import java.io.IOException;

public interface ImageService {
    CloudinaryProductImageDTO uploadImage(MultipartFile file) throws IOException;

    void deleteImage(String publicId) throws IOException;
}
