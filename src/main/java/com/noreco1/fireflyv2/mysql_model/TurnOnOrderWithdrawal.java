package com.noreco1.fireflyv2.mysql_model;

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
@EqualsAndHashCode
@Entity
public class TurnOnOrderWithdrawal implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Date date;

    @Column(updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @Transient
    private Integer totalTurnOnOrders;

    public TurnOnOrderWithdrawal(){}

    public TurnOnOrderWithdrawal(Date date, Date createdAt, Date updatedAt, Integer totalTurnOnOrders) {
        this.date = date;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalTurnOnOrders = totalTurnOnOrders;
    }

}
