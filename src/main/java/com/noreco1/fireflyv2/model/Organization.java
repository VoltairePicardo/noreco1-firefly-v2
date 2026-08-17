package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String name;

    @Column
    private String acronym;

    @Column
    private String contact;

    @Column
    private String url;

    @Column
    private String address;

    @Column
    private String email;

    @Column
    private String tin;

    @Column
    private String rdo;

    @Column
    private String zipCode;

    public Organization(String name, String acronym, String contact, String url, String address, String email,
                        String tin, String rdo) {
        this.name = name;
        this.acronym = acronym;
        this.contact = contact;
        this.url = url;
        this.address = address;
        this.email = email;
        this.tin = tin;
        this.rdo = rdo;
    }

}
