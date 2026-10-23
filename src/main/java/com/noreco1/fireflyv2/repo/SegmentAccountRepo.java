package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SegmentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SegmentAccountRepo extends JpaRepository<SegmentAccount, Integer> {
    @Query("SELECT sa FROM SegmentAccount sa WHERE sa.account.id = :accountId")
    List<SegmentAccount> findByAccountId(@Param("accountId") Integer accountId);
    @Transactional(readOnly = true)

    SegmentAccount findOneByAccountIdAndBusinessSegmentId(Integer accountId, Integer businessSegmentId);
}
