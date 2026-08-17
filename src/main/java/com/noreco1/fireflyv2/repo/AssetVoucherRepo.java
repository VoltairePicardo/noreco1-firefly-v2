package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssetVoucher;
import com.noreco1.fireflyv2.model.AssetVoucherLinkType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetVoucherRepo extends JpaRepository<AssetVoucher, Integer> {
    List<AssetVoucher> findByAssetId(Integer assetId);
}
