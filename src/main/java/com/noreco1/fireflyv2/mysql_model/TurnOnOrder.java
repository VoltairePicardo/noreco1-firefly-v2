package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
public class TurnOnOrder implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Date toDate;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="FK_consumerId")
    private Consumer consumer;

    public TurnOnOrder(){}

    public TurnOnOrder(Date toDate, Consumer consumer) {
        this.toDate = toDate;
        this.consumer = consumer;
    }

}
