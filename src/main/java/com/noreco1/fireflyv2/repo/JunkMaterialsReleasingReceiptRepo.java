package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JunkMaterialsReleasingReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JunkMaterialsReleasingReceiptRepo extends JpaRepository<JunkMaterialsReleasingReceipt, Integer> {
    JunkMaterialsReleasingReceipt findByDocId(Integer docId);
}
