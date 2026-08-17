package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class CheckConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column(nullable = false)
    String code;

    @Column(nullable = false)
    Integer dateX;

    @Column(nullable = false)
    Integer dateY;

    @Column(nullable = false)
    Integer payeeX;

    @Column(nullable = false)
    Integer payeeY;

    @Column(nullable = false)
    Integer payeeW;

    @Column(nullable = false)
    Integer numericAmountX;

    @Column(nullable = false)
    Integer numericAmountY;

    @Column(nullable = false)
    Integer alphaAmountX;

    @Column(nullable = false)
    Integer alphaAmountY;

    @Column(nullable = false)
    Integer alphaAmountW;

    @Column(nullable = false)
    Integer sig1X;

    @Column(nullable = false)
    Integer sig1Y;

    @Column(nullable = false)
    Integer sig2X;

    @Column(nullable = false)
    Integer sig2Y;

    @Column(nullable = false)
    Integer checkNoX;

    @Column(nullable = false)
    Integer checkNoY;

    @Column(nullable = false)
    String checkNoPrefix;

    @Column(nullable = false)
    Boolean withSigner;

    @Column(nullable = false)
    Boolean showDesignation;

    @Column
    String dateFormat;

    @Column
    Integer dateLineSpacing;

    @Column
    Integer amountNoOfCharToAdjust;

    @Column
    Integer nameNoOfCharToAdjust;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_accountId")
    private Account account;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_bankAccountId")
    private BankAccount bankAccount;

}
