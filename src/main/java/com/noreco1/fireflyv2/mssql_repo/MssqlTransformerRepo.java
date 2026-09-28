package com.noreco1.fireflyv2.mssql_repo;

import com.noreco1.fireflyv2.mssql_model.Transformer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MssqlTransformerRepo extends JpaRepository<Transformer, Integer> {
}
