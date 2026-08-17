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
public class PettyCashTransBudgetDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_pettyCashTransId")
    private PettyCashTrans pettyCashTrans;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_cashflowItemId")
    private CashflowItem cashflowItem;

    @Column
    private BigDecimal amount;

    @Transient
    private String parent;

    public PettyCashTransBudgetDetail(PettyCashTrans pettyCashTrans, CashflowItem cashflowItem, BigDecimal amount, String parent) {
        this.pettyCashTrans = pettyCashTrans;
        this.cashflowItem = cashflowItem;
        this.amount = amount;
        this.parent = parent;
    }

}
