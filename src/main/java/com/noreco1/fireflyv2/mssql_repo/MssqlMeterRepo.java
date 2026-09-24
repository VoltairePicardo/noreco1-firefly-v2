package com.noreco1.fireflyv2.mssql_repo;

import com.noreco1.fireflyv2.mssql_model.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MssqlMeterRepo extends JpaRepository<Meter, Integer> {
    Optional<Meter> findFirstBySerialNoOrderByIdDesc(String serialNo);
}
