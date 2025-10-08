package com.ecommerce.bkd.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.bkd.exceptions.ResourceNotFoundException;
import com.ecommerce.bkd.models.Content;
import com.ecommerce.bkd.repositories.ContentRepository;
import com.ecommerce.bkd.services.ContentService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;

    @Override
    public Content createContent(Content content) {
        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(LocalDateTime.now());
        return contentRepository.save(content);
    }

    @Override
    public Content getContent(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));
    }

    @Override
    public Content getContentByKey(String key) {
        return contentRepository.findByContentKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with key: " + key));
    }

    @Override
    public List<Content> getAllContent() {
        return contentRepository.findAll();
    }

    @Override
    public Content updateContent(Long id, Content updatedContent) {
        Content content = getContent(id);
        content.setContentKey(updatedContent.getContentKey());
        content.setContent(updatedContent.getContent());
        content.setBanners(updatedContent.getBanners());
        content.setUpdatedAt(LocalDateTime.now());
        return contentRepository.save(content);
    }

    @Override
    public void deleteContent(Long id) {
        if (!contentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Content not found with id: " + id);
        }
        contentRepository.deleteById(id);
    }
}