package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Mrct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MrctRepo extends JpaRepository<Mrct, Integer> {
    Mrct findByDocId(Integer docId);
}
