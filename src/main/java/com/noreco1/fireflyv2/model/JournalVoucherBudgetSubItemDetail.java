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
public class JournalVoucherBudgetSubItemDetail implements Serializable {

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
    @JoinColumn(name = "FK_budgetSubItemId")
    private BudgetSubItem budgetSubItem;

    @Column
    private BigDecimal amount;

    @Column
    private BigDecimal budgetSubItemAmountBalanceCV;

    @Column
    private BigDecimal budgetSubItemAmountBalancePOJO;
}
