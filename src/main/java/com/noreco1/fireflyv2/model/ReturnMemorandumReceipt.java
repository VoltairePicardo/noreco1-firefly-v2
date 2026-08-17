package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ReturnMemorandumReceipt extends DocumentNoApproval implements Serializable {

    @Column
    private Date date;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_memorandumReceiptId")
    private MemorandumReceipt memorandumReceipt;

    @Column
    private String remarks;

    @Transient
    private ArrayList<ReturnMemorandumReceiptDetail> returnMemorandumReceiptDetails = new ArrayList<>();

}
