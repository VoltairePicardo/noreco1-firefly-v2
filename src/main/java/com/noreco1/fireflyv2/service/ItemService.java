package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {
    Page<Item> list(String q, Integer accountId, Integer categoryId, int page, int size);
    Page<Item> findAll(Pageable pageable);
    Item findById(Integer id);
    PostResponse create(Item item);
    PostResponse update(Item item);
    PostResponse deleteById(Integer id);
}
