package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Current;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurrentRepo extends JpaRepository<Current, Integer> {
    List<Current> findByOrderByDescriptionAsc();
}
