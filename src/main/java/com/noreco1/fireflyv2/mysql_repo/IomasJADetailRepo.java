package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasJADetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IomasJADetailRepo extends JpaRepository<IomasJADetail, Integer> {
    @Query("SELECT d FROM JADetails d WHERE d.hId = :hId")
    List<IomasJADetail> findAllByHId(@Param("hId") Integer hId);
}
