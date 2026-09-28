package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.TransformerTesting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformerTestingRepo extends JpaRepository<TransformerTesting, Integer> {

    Page<TransformerTesting> findAllByTransformerSerialNoContainingIgnoreCase(String transformerSerialNo, Pageable pageable);

    TransformerTesting findByTransactionId(Integer id);

}
