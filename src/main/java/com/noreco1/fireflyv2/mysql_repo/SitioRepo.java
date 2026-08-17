package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.Sitio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SitioRepo extends JpaRepository<Sitio, Integer> {

    List<Sitio> findAllByBrgyIdOrderBySitioName(Integer brgyId);

}
