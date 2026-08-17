package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.StatementOfAccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatementOfAccountDetailRepo extends JpaRepository<StatementOfAccountDetail, Integer> {
    List<StatementOfAccountDetail> findAllByStatementOfAccountId(Integer id);
}
