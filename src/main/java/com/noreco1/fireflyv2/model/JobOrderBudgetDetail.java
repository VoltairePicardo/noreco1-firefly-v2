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
public class JobOrderBudgetDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_jobOrderId")
    private JobOrder jobOrder;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_budgetSubItemId")
    private BudgetSubItem budgetSubItem;

    @Column
    private BigDecimal amount;

    @Transient
    private String parent;

    @Column
    private BigDecimal budgetSubItemAmountBalanceCV;

    @Column
    private BigDecimal budgetSubItemAmountBalancePOJO;

    public JobOrderBudgetDetail(JobOrder jobOrder, BudgetSubItem budgetSubItem, BigDecimal amount, String parent, BigDecimal budgetSubItemAmountBalanceCV, BigDecimal budgetSubItemAmountBalancePOJO) {
        this.jobOrder = jobOrder;
        this.budgetSubItem = budgetSubItem;
        this.amount = amount;
        this.parent = parent;
        this.budgetSubItemAmountBalanceCV = budgetSubItemAmountBalanceCV;
        this.budgetSubItemAmountBalancePOJO = budgetSubItemAmountBalancePOJO;
    }

}
