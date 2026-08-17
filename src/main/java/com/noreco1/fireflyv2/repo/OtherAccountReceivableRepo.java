package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.OtherAccountReceivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Personal on 1/26/2016.
 */
public interface OtherAccountReceivableRepo extends JpaRepository<OtherAccountReceivable, Integer> {
    @Query(value = "SELECT e.code FROM OtherAccountReceivable e WHERE year = :year  AND code LIKE '%OAR%' AND code LIKE :offAcro ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCodeByYear(@Param("year") Integer year, @Param("offAcro") String offAcro);

    public List<OtherAccountReceivable> findByDocumentStatusId(Integer statusId);

    public OtherAccountReceivable findOneByTransactionId(Integer transId);
    OtherAccountReceivable findFirstByOrderByIdDesc();
}
