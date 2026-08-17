package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasLIDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IomasLIDetailRepo extends JpaRepository<IomasLIDetail, Integer> {
    @Query("SELECT d FROM LIDetail d WHERE d.hId = :hId")
    List<IomasLIDetail> findAllByHId(@Param("hId") Integer hId);
}
