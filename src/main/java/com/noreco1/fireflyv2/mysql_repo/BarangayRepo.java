package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.Barangay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarangayRepo extends JpaRepository<Barangay, Integer> {

    List<Barangay> findAllByTownIdOrderByBrgyName(Integer townId);

}
