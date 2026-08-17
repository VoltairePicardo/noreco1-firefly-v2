package com.noreco1.fireflyv2.mysql_repo;

import com.noreco1.fireflyv2.mysql_model.Town;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MSSQLTownRepo extends JpaRepository<Town, Integer> {
}
