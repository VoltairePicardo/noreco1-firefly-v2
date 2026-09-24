package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Phase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhaseRepo extends JpaRepository<Phase, Integer> {
    List<Phase> findByOrderByDescriptionAsc();
}
