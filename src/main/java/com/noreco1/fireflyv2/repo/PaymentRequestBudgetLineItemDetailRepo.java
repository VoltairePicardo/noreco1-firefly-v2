package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PaymentRequestBudgetLineItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface PaymentRequestBudgetLineItemDetailRepo extends JpaRepository<PaymentRequestBudgetLineItemDetail, Integer> {

    @Transactional
    public Long deleteByPaymentRequestId(Integer transId);

    ArrayList<PaymentRequestBudgetLineItemDetail> findAllByPaymentRequestId(Integer id);

}
