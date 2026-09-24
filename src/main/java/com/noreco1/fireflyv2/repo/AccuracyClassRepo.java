package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AccuracyClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccuracyClassRepo extends JpaRepository<AccuracyClass, Integer> {
    List<AccuracyClass> findByOrderByDescriptionAsc();
}
