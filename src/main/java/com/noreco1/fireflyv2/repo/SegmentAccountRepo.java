package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SegmentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SegmentAccountRepo extends JpaRepository<SegmentAccount, Integer> {
    public List<SegmentAccount> findByAccountId(Integer accountId);
    @Transactional(readOnly = true)

    SegmentAccount findOneByAccountIdAndBusinessSegmentId(Integer accountId, Integer businessSegmentId);
}
