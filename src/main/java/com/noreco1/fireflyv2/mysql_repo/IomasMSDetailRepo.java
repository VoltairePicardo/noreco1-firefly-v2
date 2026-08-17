package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.IomasMCDetail;
import com.noreco1.fireflyv2.mysql_model.IomasMSDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IomasMSDetailRepo extends JpaRepository<IomasMSDetail, Integer> {
    List<IomasMSDetail> findAllByMshId(Integer mshId);
}
