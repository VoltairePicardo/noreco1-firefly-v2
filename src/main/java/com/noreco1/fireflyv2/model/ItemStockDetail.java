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
public class ItemStockDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_itemStockId")
    private ItemStock itemStock;

    @Column
    private BigDecimal quantity;

    @Column
    private BigDecimal unitCost;

    @Column
    private BigDecimal itemCost;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_rrDetailId")
    private ReceivingReportDetail receivingReportDetail;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_debitAccountId")
    private Account debitAccount;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_creditAccountId")
    private Account creditAccount;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_departmentId")
    private Department department;

    public ItemStockDetail(ItemStock itemStock, BigDecimal quantity, BigDecimal unitCost, BigDecimal itemCost, ReceivingReportDetail receivingReportDetail, Account debitAccount, Account creditAccount, Department department) {
        this.itemStock = itemStock;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.itemCost = itemCost;
        this.receivingReportDetail = receivingReportDetail;
        this.debitAccount = debitAccount;
        this.creditAccount = creditAccount;
    }

}
