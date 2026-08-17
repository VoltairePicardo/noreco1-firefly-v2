package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MrctDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MrctDetailRepo extends JpaRepository<MrctDetail, Integer> {
    List<MrctDetail> findAllByMrctId(Integer mrctId);
}
