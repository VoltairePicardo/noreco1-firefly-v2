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
public class Sitio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer sitioID;

    @Column
    private String sitioName;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="BrgyID")
    private Barangay brgy;

    public Sitio(){}

    public Sitio(String sitioName, Barangay brgy) {
        this.sitioName = sitioName;
        this.brgy = brgy;
    }

}
