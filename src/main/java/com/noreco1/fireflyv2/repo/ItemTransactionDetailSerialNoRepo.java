package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ItemTransactionDetailSerialNo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemTransactionDetailSerialNoRepo extends JpaRepository<ItemTransactionDetailSerialNo, Integer>{
    void deleteByItemTransactionDetailId(Integer itemTransactionDetailId);
    List<ItemTransactionDetailSerialNo> findAllByItemTransactionDetailId(Integer itemTransactionDetailId);
}
