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
public class TurnOnAccomplishment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="toID")
    private TurnOnOrder turnOnOrder;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date installDate;

    public TurnOnAccomplishment(){}

    public TurnOnAccomplishment(TurnOnOrder turnOnOrder, Date installDate) {
        this.turnOnOrder = turnOnOrder;
        this.installDate = installDate;
    }

}
