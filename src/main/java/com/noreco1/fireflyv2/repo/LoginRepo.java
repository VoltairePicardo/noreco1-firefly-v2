package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Account;
import com.noreco1.fireflyv2.model.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LoginRepo extends JpaRepository<Login, Integer> {
}
