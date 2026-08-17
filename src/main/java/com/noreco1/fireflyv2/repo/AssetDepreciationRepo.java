package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssetDepreciation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetDepreciationRepo extends JpaRepository<AssetDepreciation, Integer> {

    List<AssetDepreciation> findAllByYearAndMonthOrderByYearDescMonthDesc(Integer year, Integer month);
    List<AssetDepreciation> findByYearAndMonth(Integer year, Integer month);

}
