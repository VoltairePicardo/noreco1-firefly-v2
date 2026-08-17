package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PettyCashLiquidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by tonyc on 6/20/2023.
 */
public interface PettyCashLiquidationRepo extends JpaRepository<PettyCashLiquidation, Integer> {
    @Query(value = "select " +
            "la.* " +
            "from PettyCashLiquidation la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id  " +
            "LEFT JOIN `User` receiveUser on la.FK_receivedByUserId = receiveUser.id " +
            "WHERE approveUser.id = :userId OR createUser.id = :userId  " +
            "OR receiveUser.id = :userId",
            nativeQuery = true)
    List<PettyCashLiquidation> findAllByAllowedUsers(@Param("userId") Integer userId);

    PettyCashLiquidation findOneByTransactionId(Integer id);

    @Query(value = "SELECT e.code FROM PettyCashLiquidation e WHERE year(now()) = :year  AND code LIKE '%PCL%'  AND code LIKE :offAcro ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year, @Param("offAcro") String offAcro);
}
