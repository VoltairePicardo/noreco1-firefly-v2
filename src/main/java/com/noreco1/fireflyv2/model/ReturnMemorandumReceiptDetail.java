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
public class ReturnMemorandumReceiptDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_returnMemorandumReceiptId")
    private ReturnMemorandumReceipt returnMemorandumReceipt;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_stockWithdrawalDetailId")
    private StockWithdrawalDetail stockWithdrawalDetail;

    @Column
    private Integer quantity;

    @Column
    private BigDecimal returnedQuantity;

    @Column
    private BigDecimal returnedToInventoryQuantity;

    @Column
    private Boolean usable;

    @Transient
    private BigDecimal reassignedQuantity;

    public ReturnMemorandumReceiptDetail(ReturnMemorandumReceipt returnMemorandumReceipt, StockWithdrawalDetail stockWithdrawalDetail, Integer quantity, BigDecimal returnedQuantity, BigDecimal returnedToInventoryQuantity, Boolean usable) {
        this.returnMemorandumReceipt = returnMemorandumReceipt;
        this.stockWithdrawalDetail = stockWithdrawalDetail;
        this.quantity = quantity;
        this.returnedQuantity = returnedQuantity;
        this.returnedToInventoryQuantity = returnedToInventoryQuantity;
        this.usable = usable;
    }

}
