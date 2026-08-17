package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.FactorPercentageDistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface FactorPercentageDistroRepo extends JpaRepository<FactorPercentageDistro, Integer> {
    Long deleteByFactorId(Integer id);
    Long deleteByFactorIdAndValidityDateId(Integer id, Integer validityDateId);
    Set<FactorPercentageDistro> findByFactorId(Integer id);
    Set<FactorPercentageDistro> findByFactorIdAndValidityDateId(Integer id, Integer validityDateId);
}
