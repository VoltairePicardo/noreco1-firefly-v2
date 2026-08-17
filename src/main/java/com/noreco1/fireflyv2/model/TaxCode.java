package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TaxCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String code;

    @Column
    private String description;

    @Column
    private BigDecimal rate;

    @Column
    private String payeeType;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_accountId")
    private Account account;

    public TaxCode(String code, String description, BigDecimal rate, String payeeType, Account account) {
        this.code = code;
        this.description = description;
        this.rate = rate;
        this.payeeType = payeeType;
        this.account = account;
    }

}
