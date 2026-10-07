package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.InventoryCategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryCategoryTypeRepo extends JpaRepository<InventoryCategoryType, Integer> {
    List<InventoryCategoryType> findAllByOrderByDescriptionAsc();
}
