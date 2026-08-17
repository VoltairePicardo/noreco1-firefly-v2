package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.model.BusinessSegment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BusinessSegmentRepo extends JpaRepository<BusinessSegment, Integer> {

    List<BusinessSegment> findAllByBusinessActivityId(Integer activityId);

}
