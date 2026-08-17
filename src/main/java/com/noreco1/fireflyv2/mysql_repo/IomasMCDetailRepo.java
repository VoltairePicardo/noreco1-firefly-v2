package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasADDetail;
import com.noreco1.fireflyv2.mysql_model.IomasMCDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IomasMCDetailRepo extends JpaRepository<IomasMCDetail, Integer> {
    List<IomasMCDetail> findAllByMchId(Integer mchId);
}
