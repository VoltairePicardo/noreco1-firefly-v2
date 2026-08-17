package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class PettyCashTrans extends Voucher {

    @Column(name = "FK_accountNo")
    private Integer accountNo;

    @Temporal(TemporalType.DATE)
    @Column
    private Date pettyCashDate;

    @Column
    private String payee;

    @Column
    private String request;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    private ArrayList<PettyCashTransDetail> pettyCashTransDetails = new ArrayList<>();

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_budgetLineItemDetailId")
    private BudgetLineItemDetail budgetLineItemDetail;

    @Column
    private BigDecimal budgetLineItemBalancePOJORFP;

    @Column
    private BigDecimal budgetLineItemBalanceCV;

    @Column
    private BigDecimal cashFlowItemBalancePOJORFP;

    @Column
    private BigDecimal cashFlowItemBalanceCV;

    @Column
    private BigDecimal cashFlowItemTotal;

    @Column
    private BigDecimal budgetAmountBalancePCL;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_pettyCashFundId")
    private PettyCashFund pettyCashFund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_releasedByUserId")
    private User releasingOfficer;


}
