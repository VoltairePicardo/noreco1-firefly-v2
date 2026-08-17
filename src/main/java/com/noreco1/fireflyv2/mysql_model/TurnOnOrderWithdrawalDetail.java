package com.noreco1.fireflyv2.mysql_model;

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
public class TurnOnOrderWithdrawalDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_turnOnOrderWithdrawalId")
    private TurnOnOrderWithdrawal turnOnOrderWithdrawal;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_turnOnOrderId")
    private TurnOnOrder turnOnOrder;

    public TurnOnOrderWithdrawalDetail(){}

    public TurnOnOrderWithdrawalDetail(TurnOnOrderWithdrawal turnOnOrderWithdrawal, TurnOnOrder turnOnOrder) {
        this.turnOnOrderWithdrawal = turnOnOrderWithdrawal;
        this.turnOnOrder = turnOnOrder;
    }

}
