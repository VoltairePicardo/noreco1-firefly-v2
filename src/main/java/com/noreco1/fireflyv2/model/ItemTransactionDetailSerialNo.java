package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ItemTransactionDetailSerialNo implements Serializable{

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_itemTransactionDetailId")
    private ItemTransactionDetail itemTransactionDetail;

    @Column
    private String serialNo;

    public ItemTransactionDetailSerialNo(ItemTransactionDetail itemTransactionDetail, String serialNo) {
        this.itemTransactionDetail = itemTransactionDetail;
        this.serialNo = serialNo;
    }

}
