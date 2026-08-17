package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PrepaymentDetail implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private BigDecimal amount;

    @Column
    private BigDecimal balance;

    @Column
    private Integer year;

    @Column
    private Integer month;

    @Column(name = "FK_accountNo")
    private Integer accountNumber;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_prepaymentId")
    private Prepayment prepayment;

    public PrepaymentDetail(BigDecimal amount, BigDecimal balance, Integer year, Integer month, Integer accountNumber, Prepayment prepayment) {
        this.amount = amount;
        this.balance = balance;
        this.year = year;
        this.month = month;
        this.accountNumber = accountNumber;
        this.prepayment = prepayment;
    }

}
