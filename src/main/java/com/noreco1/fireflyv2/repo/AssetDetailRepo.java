package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AssetDetailRepo extends JpaRepository<AssetDetail, Integer> {

    Long deleteByAssetId(Integer assetId);
    List<AssetDetail> findByAssetId(Integer assetId);

    List<AssetDetail> findByAssetIdAndAssetAccountIsNotNull(Integer assetId);

}
