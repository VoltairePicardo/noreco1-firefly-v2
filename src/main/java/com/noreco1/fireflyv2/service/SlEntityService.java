package com.noreco1.fireflyv2.service;

import com.noreco1.fireflyv2.model.Item;
import com.noreco1.fireflyv2.model.SlEntity;
import com.noreco1.fireflyv2.model.SubLedgerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SlEntityService extends DataManagementService {
    // View
    public List<SlEntity> findAll();
    public List<SlEntity> findAllByType(Integer[] entityTypes);

    // Table
    public List<SubLedgerEntity> findAllStrong();

    public Page<SlEntity> findAll(Pageable pageable);
    public Page<SlEntity> findByQuery(String query, Pageable pageable);
    public Page<SlEntity> findByQueryAndTypes(String query, Integer[] entityTypes, Pageable pageable);

    public Page<SlEntity> findByClassificationQuery(String classification, String query, Pageable pageable);
    public Page<SlEntity> findByClassificationQueryAndTypes(String classification, String query, Integer[] entityTypes, Pageable pageable);

    public Page<SubLedgerEntity> findAllStrong(Pageable pageable);

    public Page<SubLedgerEntity> findStrongByQuery(String query, Pageable pageable);

    public Page<SubLedgerEntity> findStrongByClassificationQuery(String classification, String query, Pageable pageable);

    Page<SlEntity> findAllByClassificationQueryLevel(String classification, Integer level, String query, Pageable pageable);
}
