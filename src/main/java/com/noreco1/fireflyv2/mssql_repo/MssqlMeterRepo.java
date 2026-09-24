package com.noreco1.fireflyv2.mssql_repo;

import com.noreco1.fireflyv2.mssql_model.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MssqlMeterRepo extends JpaRepository<Meter, Integer> {
}
