package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Transformer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformerRepo extends JpaRepository<Transformer, Integer> {

    Page<Transformer> findAllBySerialNoContainingIgnoreCase(String serialNo, Pageable pageable);
}
