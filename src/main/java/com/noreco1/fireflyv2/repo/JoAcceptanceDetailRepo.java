package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JoAcceptanceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Personal on 7/7/2015.
 */
public interface JoAcceptanceDetailRepo extends JpaRepository<JoAcceptanceDetail, Integer> {
    @Transactional
    @Query(value = "SELECT joad.* FROM JoAcceptanceDetail joad " +
            "INNER JOIN JoDetail jod ON joad.FK_joDetailId = jod.id " +
            "INNER JOIN PurchaseRequestDetail rvd ON jod.FK_PurchaseRequestDetailId = rvd.id " +
            "WHERE joad.FK_joAcceptanceId = :joaId " +
            "ORDER BY rvd.id", nativeQuery = true)
    public List<JoAcceptanceDetail> findAllByJoAcceptanceId(@Param("joaId") Integer id);

    @Transactional
    public Long deleteByJoAcceptanceId(Integer transId);

    List<JoAcceptanceDetail> findByJoAcceptanceId(Integer id);
}
