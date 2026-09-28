package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.TransformerCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransformerConditionRepo extends JpaRepository<TransformerCondition, Integer> {
    List<TransformerCondition> findByOrderByDescriptionAsc();
}
