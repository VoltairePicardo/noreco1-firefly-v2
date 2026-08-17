package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.InventoryLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryLocationRepo extends JpaRepository<InventoryLocation, Integer> {
    Page<InventoryLocation> findByDescriptionContainingIgnoreCase(String query, Pageable pageable);
    InventoryLocation findByDescriptionContainingIgnoreCase(String query);
    List<InventoryLocation> findAllByOrderByDescriptionAsc();
}
