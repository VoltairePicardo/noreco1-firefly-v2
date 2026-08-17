package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssetWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Tri-Nvent on 1/14/2020.
 */
public interface AssetWorkRepo extends JpaRepository<AssetWork, Integer> {

    Long deleteAllByAssetId(Integer assetId);
    List<AssetWork> findAllByAssetId(Integer assetId);

}
