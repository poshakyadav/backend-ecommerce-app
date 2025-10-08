package com.ecommerce.bkd.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public List<String> uploadImages
    (List<MultipartFile> files, String folder) {
        List<String> imageUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        ObjectUtils.asMap("folder", folder));
                imageUrls.add((String) uploadResult.get("url"));
            }
            return imageUrls;
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
        }
    }

    public Map<String, List<String>> uploadMultipleImages(List<MultipartFile> files, String folder) {
        List<String> imageUrls = new ArrayList<>();
        List<String> publicIds = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        ObjectUtils.asMap("folder", folder));
                imageUrls.add((String) uploadResult.get("url"));
                publicIds.add((String) uploadResult.get("public_id"));
            }
            return Map.of("urls", imageUrls, "publicIds", publicIds);
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
        }
    }

    public String uploadSingleImage(MultipartFile file, String folder) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", folder));
            return (String) uploadResult.get("url");
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
        }
    }

    // public void deleteImages(List<String> imageUrls) {
    //     try {
    //         for (String url : imageUrls) {
    //             // Extract public ID from URL
    //             // Example URL: https://res.cloudinary.com/your_cloud_name/image/upload/v1234567890/folder/image_id.jpg
    //             String publicId = extractPublicId(url);
    //             if (publicId != null) {
    //                 cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    //             }
    //         }
    //     } catch (IOException e) {
    //         throw new RuntimeException("Image deletion failed: " + e.getMessage(), e);
    //     }
    // }

    public void deleteImages(List<String> publicIds) {
        try {
            for (String publicId : publicIds) {
                if (publicId != null && !publicId.isEmpty()) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Image deletion failed: " + e.getMessage(), e);
        }
    }

    // This method deletes a single image by its URL
    // It extracts the public ID from the URL and calls Cloudinary's destroy method
    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (IOException e) {
            throw new RuntimeException("Image deletion failed: " + e.getMessage(), e);
        }
    }

    private String extractPublicId(String url) {
        // Find the part after "/upload/" and before the file extension
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) return null;
        String path = url.substring(uploadIndex + "/upload/".length());
        // Remove version (e.g., v1234567890/) if present
        if (path.startsWith("v")) {
            int slashIndex = path.indexOf("/");
            if (slashIndex != -1) {
                path = path.substring(slashIndex + 1);
            }
        }
        // Remove file extension (e.g., .jpg)
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex != -1) {
            path = path.substring(0, dotIndex);
        }
        return path;
    }
}