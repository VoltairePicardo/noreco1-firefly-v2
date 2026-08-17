package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.JunkMaterialsReleasingReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JunkMaterialsReleasingReceiptDetailRepo extends JpaRepository<JunkMaterialsReleasingReceiptDetail, Integer> {
    List<JunkMaterialsReleasingReceiptDetail> findAllByJunkMaterialsReleasingReceiptId(Integer id);
}
