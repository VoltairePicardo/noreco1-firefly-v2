package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepo extends JpaRepository<Brand, Integer> {
    List<Brand> findByOrderByName();
    Brand findTop1ByNameIgnoreCase(String q);
    Page<Brand> findByOrderByName(Pageable pageable);
    Page<Brand> findByNameContainingIgnoreCase(String q, Pageable pageable);
    List<Brand> findByNameContainingIgnoreCase(String q);
}
