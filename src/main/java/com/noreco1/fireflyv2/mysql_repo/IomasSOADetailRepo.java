package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasSOADetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IomasSOADetailRepo extends JpaRepository<IomasSOADetail, Integer> {
    @Query("SELECT d FROM SADetail d WHERE d.hId = :hId")
    List<IomasSOADetail> findAllByHId(@Param("hId") Integer hId);
}
