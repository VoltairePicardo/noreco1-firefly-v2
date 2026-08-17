package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetTypeRepo extends JpaRepository<AssetType, Integer> {
    Page<AssetType> findAll(Pageable pageable);
    Page<AssetType> findByDescriptionContainingIgnoreCase(String query, Pageable pageable);
    AssetType findByDescriptionContainingIgnoreCase(String query);
    List<AssetType> findByOrderByDescriptionAsc();
}
