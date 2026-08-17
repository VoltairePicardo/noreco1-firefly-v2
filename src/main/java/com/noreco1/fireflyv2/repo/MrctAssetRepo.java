package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MrctAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MrctAssetRepo extends JpaRepository<MrctAsset, Integer> {
    List<MrctAsset> findByAssetId(Integer assetId);
}
