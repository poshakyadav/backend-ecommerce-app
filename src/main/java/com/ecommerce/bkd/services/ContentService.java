package com.ecommerce.bkd.services;

import java.util.List;

import com.ecommerce.bkd.models.Content;

public interface ContentService {
    Content createContent(Content content);

    Content getContent(Long id);

    Content getContentByKey(String key);

    List<Content> getAllContent();

    Content updateContent(Long id, Content updatedContent);

    void deleteContent(Long id);
}
