package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.VehicleInformation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Tri-Nvent on 1/14/2020.
 */
public interface VehicleInformationRepo extends JpaRepository<VehicleInformation, Integer> {
    VehicleInformation findByAssetId(Integer assetId);
}
