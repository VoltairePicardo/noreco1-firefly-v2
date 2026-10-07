package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.InventorySubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventorySubCategoryRepo extends JpaRepository<InventorySubCategory, Integer> {
    List<InventorySubCategory> findByInventoryCategoryIdOrderByDescriptionAsc(Integer inventoryCategoryId);
    InventorySubCategory findFirstByInventoryCategoryIdAndDescriptionIgnoreCase(Integer inventoryCategoryId, String description);
}
