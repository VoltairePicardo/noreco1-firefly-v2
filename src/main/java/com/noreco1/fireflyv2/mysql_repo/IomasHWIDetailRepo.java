package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasHWIDetail;
import com.noreco1.fireflyv2.mysql_model.IomasJRDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IomasHWIDetailRepo extends JpaRepository<IomasHWIDetail, Integer> {
    List<IomasHWIDetail> findAllByMrhId(Integer mrhId);
}
