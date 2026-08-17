package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasMRDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IomasMRDetailRepo extends JpaRepository<IomasMRDetail, Integer> {
    List<IomasMRDetail> findAllByMrhId(Integer mrhId);
}
