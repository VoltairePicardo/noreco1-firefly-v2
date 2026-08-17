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
public class Barangay implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String brgyCode;

    @Column
    private String brgyName;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="FK_townId")
    private Town town;

    public Barangay(){}

    public Barangay(String brgyCode, String brgyName, Town town) {
        this.brgyCode = brgyCode;
        this.brgyName = brgyName;
        this.town = town;
    }

}
