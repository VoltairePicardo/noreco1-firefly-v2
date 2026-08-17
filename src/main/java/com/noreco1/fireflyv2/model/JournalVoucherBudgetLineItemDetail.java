package com.noreco1.fireflyv2.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalVoucherBudgetLineItemDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_journalVoucherId")
    private JournalVoucher journalVoucher;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_budgetLineItemDetailId")
    private BudgetLineItemDetail budgetLineItemDetail;

    @Column
    private BigDecimal budgetAmountBalancePOJORFP;

    @Column
    private BigDecimal budgetAmountBalanceCV;
}
