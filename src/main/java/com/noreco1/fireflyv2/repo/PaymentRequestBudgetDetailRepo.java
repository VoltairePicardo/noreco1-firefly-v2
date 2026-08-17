package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PaymentRequestBudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentRequestBudgetDetailRepo extends JpaRepository<PaymentRequestBudgetDetail, Integer> {

    @Transactional
    public Long deleteByPaymentRequestId(Integer transId);

    @Transactional
    List<PaymentRequestBudgetDetail> findAllByPaymentRequestId(Integer poId);

    PaymentRequestBudgetDetail findFirstByPaymentRequestIdOrderByIdAsc(Integer poId);

}
