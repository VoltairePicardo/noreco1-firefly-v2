package com.noreco1.fireflyv2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherCashflowDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "FK_transactionId") // transaction id of the voucher
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "FK_cashflowItemId")
    private CashflowItem cashflowItem;

    @Column
    @Temporal(TemporalType.DATE)
    private Date voucherDate;

    @Column
    private BigDecimal amount;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_generalLedgerId")
    private GeneralLedger generalLedger;
}
