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
public class PaymentRequestBudgetDetail implements Serializable {

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
    @JoinColumn(name = "FK_budgetSubItemId")
    private BudgetSubItem budgetSubItem;

    @Column
    private BigDecimal amount;

    @Transient
    private String parent;

    public PaymentRequestBudgetDetail(PaymentRequest paymentRequest, BudgetSubItem budgetSubItem, BigDecimal amount) {
        this.paymentRequest = paymentRequest;
        this.budgetSubItem = budgetSubItem;
        this.amount = amount;
    }

}
