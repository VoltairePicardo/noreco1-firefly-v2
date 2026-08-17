package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasSADetail;
import com.noreco1.fireflyv2.mysql_model.IomasSMDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IomasSMDetailRepo extends JpaRepository<IomasSMDetail, Integer> {
    List<IomasSMDetail> findAllBySmhId(Integer smhId);
}
