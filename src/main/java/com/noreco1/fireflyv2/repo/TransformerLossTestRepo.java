package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.TransformerLossTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransformerLossTestRepo extends JpaRepository<TransformerLossTest, Integer> {

    List<TransformerLossTest> findAllByTransformerTestingId(Integer id);

    TransformerLossTest findByTransformerTestingId(Integer id);
}
