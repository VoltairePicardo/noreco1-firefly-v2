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
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class CashAdvance extends Voucher {

    @Column(name = "FK_accountNo")
    private Integer accountNo;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_employeeId")
    private Employee employee;

    @Temporal(TemporalType.DATE)
    @Column
    private Date cashAdvanceDate;

    @Column
    private String purpose;

    @Column
    private String remarks;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_recommendedByUserId", nullable = true, columnDefinition = "0")
    private User recommendedBy;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<CashAdvanceParticular> cashAdvanceParticulars = new ArrayList<>();

    @Column
    private boolean isLiquidated = false;

    @Column
    private Date periodCoveredFrom;

    @Column
    private Date periodCoveredTo;

    @Column
    private String location;

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

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_budgetOfficerUserId")
    private User budgetOfficer;

}
