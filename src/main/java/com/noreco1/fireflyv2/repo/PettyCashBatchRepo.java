package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PettyCashBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PettyCashBatchRepo extends JpaRepository<PettyCashBatch, Integer> {
    PettyCashBatch findByStatus(Boolean status);
    PettyCashBatch findByStatusAndOfficeId(Boolean status, int officeId);

    List<PettyCashBatch> findAllByOfficeId(Integer officeId);

    @Query(value = "select * from PettyCashBatch " +
            "WHERE (createdAt >= :from AND createdAt <= :to) " +
            "AND FK_areaOfficeId = :officeId ", nativeQuery = true)
    List<PettyCashBatch> findAllByOfficeAndDateRange(@Param("from") String from, @Param("to") String to, @Param("officeId") Integer officeId);
}
