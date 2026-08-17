package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class MiscellaneousCharge implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_accountId")
    private Account account;

    @Column
    private BigDecimal amount;

    @Column
    private Boolean vatable;

    public MiscellaneousCharge(String description, Account account, BigDecimal amount, Boolean vatable) {
        this.description = description;
        this.account = account;
        this.amount = amount;
        this.vatable = vatable;
    }

}
