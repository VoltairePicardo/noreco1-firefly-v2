package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasJRDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IomasJRDetailRepo extends JpaRepository<IomasJRDetail, Integer> {
    @Query("SELECT d FROM JRDetails d WHERE d.hId = :hId")
    List<IomasJRDetail> findAllByHId(@Param("hId") Integer hId);
}
