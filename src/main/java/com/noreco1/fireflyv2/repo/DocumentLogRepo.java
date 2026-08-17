package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.DocumentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentLogRepo extends JpaRepository<DocumentLog, Integer> {
    public List<DocumentLog> findAllByTransactionIdOrderByCreatedAtDesc(Integer transId);
}