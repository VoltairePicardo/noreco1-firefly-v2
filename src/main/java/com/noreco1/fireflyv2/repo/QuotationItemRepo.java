package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationItemRepo extends JpaRepository<QuotationItem, Integer> {
    List<QuotationItem> findAllByQuotationId(Integer id);
    long deleteByQuotationId(Integer qid);
}
