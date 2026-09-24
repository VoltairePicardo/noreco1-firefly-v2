package com.noreco1.fireflyv2.mssql_repo;

import com.noreco1.fireflyv2.mssql_model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MssqlUserRepo extends JpaRepository<User, Integer> {
    List<User> findAllByAccountNumberIn(List<Integer> accountNumbers);
}
