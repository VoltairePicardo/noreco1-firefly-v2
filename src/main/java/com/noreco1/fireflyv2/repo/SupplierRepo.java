package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SupplierRepo extends JpaRepository<Supplier, Integer> {
    List<Supplier> findByName(String name);

    Supplier findOneByAccountNumber(Integer accountNo);

    @Transactional
    Page<Supplier> findAllByOrderByName(Pageable pageable);

    Page<Supplier> findByNameContainingIgnoreCaseOrderByName(String q, Pageable pageable);

    @Query(value = "SELECT " +
            "* " +
            "FROM Supplier " +
            "WHERE MATCH(Supplier.name) AGAINST(:fullTextQuery IN BOOLEAN MODE) " +
            "ORDER BY Supplier.name DESC LIMIT 10 ", nativeQuery = true)
    List<Supplier> findAllByName(@Param("fullTextQuery") String fullTextQuery);

}
