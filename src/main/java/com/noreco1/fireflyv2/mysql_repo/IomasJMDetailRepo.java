package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasJMDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IomasJMDetailRepo extends JpaRepository<IomasJMDetail, Integer> {
    @Query("SELECT d FROM JMDetails d WHERE d.hId = :hId")
    List<IomasJMDetail> findAllByHId(@Param("hId") Integer hId);
}
