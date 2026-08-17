package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssemblyType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssemblyTypeRepo extends JpaRepository<AssemblyType, Integer> {
    AssemblyType findByDescription(String description);
}
