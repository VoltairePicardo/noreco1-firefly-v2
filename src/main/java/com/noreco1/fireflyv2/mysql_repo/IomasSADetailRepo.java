package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasMSDetail;
import com.noreco1.fireflyv2.mysql_model.IomasSADetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IomasSADetailRepo extends JpaRepository<IomasSADetail, Integer> {
    List<IomasSADetail> findAllBySahId(Integer sahId);
}
