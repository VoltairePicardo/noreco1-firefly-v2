package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.TransformerVoltageRatioTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransformerVoltageRatioTestRepo extends JpaRepository<TransformerVoltageRatioTest, Integer> {

    List<TransformerVoltageRatioTest> findAllByTransformerTestingId(Integer id);

    TransformerVoltageRatioTest findByTransformerTestingId(Integer id);
}
