package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ItemTransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public interface ItemTransactionDetailRepo extends JpaRepository<ItemTransactionDetail, Integer> {
    ArrayList<ItemTransactionDetail> findByTransactionId(Integer transId);

    void deleteByTransactionId(Integer transId);

    List<ItemTransactionDetail> findAllByMemorandumReceiptDetailMemorandumReceiptId(int id);
    List<ItemTransactionDetail> findAllByMemorandumReceiptDetail_MemorandumReceiptId(int id);

    ItemTransactionDetail findTopByTransactionIdAndItemIdOrderByIdDesc(Integer transactionId, Integer itemId);

}
