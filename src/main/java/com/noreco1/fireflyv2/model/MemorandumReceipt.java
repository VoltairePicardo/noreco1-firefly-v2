package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MemorandumReceipt extends Document implements Serializable {

    @Column
    private Date date;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_stockWithdrawalId")
    private StockWithdrawal stockWithdrawal;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_employeeAccountNo")
    private SlEntity employee;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_returnMemorandumReceiptId")
    private ReturnMemorandumReceipt returnMemorandumReceipt;

    @Transient
    private List<MemorandumReceiptDetail> memorandumReceiptDetails = new ArrayList<>();

    @Transient
    private boolean hasMst;

    public MemorandumReceipt(Date date, StockWithdrawal stockWithdrawal, SlEntity employee, ReturnMemorandumReceipt returnMemorandumReceipt, List<MemorandumReceiptDetail> memorandumReceiptDetails) {
        this.date = date;
        this.stockWithdrawal = stockWithdrawal;
        this.employee = employee;
        this.memorandumReceiptDetails = memorandumReceiptDetails;
        this.returnMemorandumReceipt = returnMemorandumReceipt;
    }

}
