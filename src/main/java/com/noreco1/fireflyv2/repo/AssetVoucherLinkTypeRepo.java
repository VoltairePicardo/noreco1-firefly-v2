package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssetVoucherLinkType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetVoucherLinkTypeRepo extends JpaRepository<AssetVoucherLinkType, Integer> {
    List<AssetVoucherLinkType> findByOrderByDescriptionAsc();
}
