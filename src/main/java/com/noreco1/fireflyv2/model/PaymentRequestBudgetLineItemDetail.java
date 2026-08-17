package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestBudgetLineItemDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_paymentRequestId")
    private PaymentRequest paymentRequest;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_budgetLineItemDetailId")
    private BudgetLineItemDetail budgetLineItemDetail;

    @Column
    private BigDecimal budgetAmountBalancePOJORFP;

    @Column
    private BigDecimal budgetAmountBalanceCV;

    public PaymentRequestBudgetLineItemDetail(PaymentRequest paymentRequest, BudgetLineItemDetail budgetLineItemDetail, BigDecimal budgetAmountBalancePOJORFP, BigDecimal budgetAmountBalanceCV) {
        this.paymentRequest = paymentRequest;
        this.budgetLineItemDetail = budgetLineItemDetail;
        this.budgetAmountBalancePOJORFP = budgetAmountBalancePOJORFP;
        this.budgetAmountBalanceCV = budgetAmountBalanceCV;
    }

}
