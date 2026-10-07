package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.InventoryCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryCategoryRepo extends JpaRepository<InventoryCategory, Integer> {
    Page<InventoryCategory> findByDescriptionContainingIgnoreCase(String query, Pageable pageable);
    InventoryCategory findFirstByDescriptionIgnoreCase(String description);
}
