package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SlEntity;
import com.noreco1.fireflyv2.model.SubLedgerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SubLedgerEntityRepo extends JpaRepository<SubLedgerEntity, Integer>{

    @Transactional(readOnly = true)
    public List<SubLedgerEntity> findAllByOrderByNameAsc();

    public Page<SubLedgerEntity> findAllByOrderByNameAsc(Pageable pageable);

    public Page<SubLedgerEntity> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrSlEntityClassificationDescriptionContainingIgnoreCaseOrderByNameAsc(String query, String query1, String query2, Pageable pageable);

    public Page<SubLedgerEntity> findByNameContainingIgnoreCaseAndSlEntityClassificationDescriptionOrderByNameAsc(String query, String classification, Pageable pageable);

    public Page<SubLedgerEntity> findBySlEntityClassificationDescriptionOrderByNameAsc(String classification, Pageable pageable);
}
