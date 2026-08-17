package com.noreco1.fireflyv2.model;

import lombok.*;

import com.noreco1.fireflyv2.controller.response.StockWithdrawalDetailDto;
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
public class MemorandumReceiptDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_MemorandumReceiptId")
    private MemorandumReceipt memorandumReceipt;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_stockWithdrawalDetailId")
    private StockWithdrawalDetail stockWithdrawalDetail;

    @Column
    private Integer quantity;

    @Column
    private BigDecimal reassignedQuantity;

    @Transient
    private BigDecimal returned;

    public MemorandumReceiptDetail(MemorandumReceipt memorandumReceipt, StockWithdrawalDetail stockWithdrawalDetail, Integer quantity, BigDecimal reassignedQuantity) {
        this.memorandumReceipt = memorandumReceipt;
        this.stockWithdrawalDetail = stockWithdrawalDetail;
        this.quantity = quantity;
        this.reassignedQuantity = reassignedQuantity;
    }

    public StockWithdrawalDetailDto toDto(){
        return new StockWithdrawalDetailDto(getStockWithdrawalDetail().getItem().getId(), getStockWithdrawalDetail().getItem().getCode(), getStockWithdrawalDetail().getUnit().getId(), getStockWithdrawalDetail().getUnit().getCode(), getStockWithdrawalDetail().getItem().getDescription(), new BigDecimal(getQuantity()), getStockWithdrawalDetail().getQuantityReleased(), new BigDecimal(getQuantity()), getStockWithdrawalDetail().getIsSpecialEquipment());
    }

}
