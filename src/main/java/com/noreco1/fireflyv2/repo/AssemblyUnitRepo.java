package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AssemblyUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssemblyUnitRepo extends JpaRepository<AssemblyUnit, Integer> {
    @Query(value = "SELECT * FROM AssemblyUnit WHERE code LIKE :query OR description LIKE :query  \n#pageable\n",
            countQuery = "SELECT * FROM AssemblyUnit WHERE code LIKE :query OR description LIKE :query",
            nativeQuery = true)
    Page<AssemblyUnit> findByQuery(@Param("query") String query, Pageable pageable);

    Page<AssemblyUnit> findAllByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByAssemblyTypeDescriptionAscCodeAsc(String query1, String query2, Pageable pageable);
    Page<AssemblyUnit> findAllByOrderByAssemblyTypeDescriptionAscCodeAsc(Pageable pageable);

}
