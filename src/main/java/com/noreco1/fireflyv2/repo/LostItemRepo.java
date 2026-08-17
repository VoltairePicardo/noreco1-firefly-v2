package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemRepo extends JpaRepository<LostItem, Integer> {
    LostItem findByDocId(Integer docId);
}
