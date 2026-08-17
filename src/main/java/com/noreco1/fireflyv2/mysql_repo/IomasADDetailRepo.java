package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasADDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IomasADDetailRepo extends JpaRepository<IomasADDetail, Integer> {
    List<IomasADDetail> findAllByAdhId(Integer mrhId);
}

