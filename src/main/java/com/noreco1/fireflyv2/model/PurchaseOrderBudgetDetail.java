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
public class PurchaseOrderBudgetDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_purchaseOrderId")
    private PurchaseOrder purchaseOrder;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_budgetSubItemId")
    private BudgetSubItem budgetSubItem;

    @Column
    private BigDecimal amount;

    @Column
    private BigDecimal budgetSubItemAmountBalanceCV;

    @Column
    private BigDecimal budgetSubItemAmountBalancePOJO;

    @Transient
    private String parent;

    public PurchaseOrderBudgetDetail(PurchaseOrder purchaseOrder, BudgetSubItem budgetSubItem, BigDecimal amount, BigDecimal budgetSubItemAmountBalanceCV, BigDecimal budgetSubItemAmountBalancePOJO) {
        this.purchaseOrder = purchaseOrder;
        this.budgetSubItem = budgetSubItem;
        this.amount = amount;
        this.budgetSubItemAmountBalanceCV = budgetSubItemAmountBalanceCV;
        this.budgetSubItemAmountBalancePOJO = budgetSubItemAmountBalancePOJO;
    }

}
