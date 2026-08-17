package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.controller.response.PoDetailDto;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
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
public class PurchaseOrder extends Document implements Serializable {

    @NotNull(message = "Please enter voucher date.")
    @Column
    private Date voucherDate;

    @Column
    private Integer year;

    @Column
    private Integer term;

    @NotNull(message = "Please select vendor.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_vendorAccountNo")
    private SlEntity vendor;

    @NotNull(message = "Total Amount must be greater than zero.")
    private BigDecimal amount = BigDecimal.ZERO;

    @NotFound(action=NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_checkedByUserId")
    private User checkedBy;

    @NotFound(action=NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_budgetCheckedByUserId")
    private User budgetCheckedBy;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<PoDetailDto> poDetails = new ArrayList<>();

    @Column
    private String deliveryTerm;

    @Column
    private String deliveryAddress;

    @Column
    private Integer paymentTerm;

    @Column
    private String purpose;

    @NotFound(action=NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_vehicleId")
    private Vehicle vehicle;

    @NotFound(action=NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_cashAdvanceId")
    private CashAdvance cashAdvance;

    @Column
    private Boolean useCreditCard = false;

    @NotFound(action=NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_departmentId")
    private Department department;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_budgetLinetItemDetailId")
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

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<PurchaseOrderBudgetDetail> budgetDetails = new ArrayList<>();

    @Column
    private String deliveryTimeAndCompletion;

    @Column
    private Date receivedDate;

    @Column
    private String receivedBy;

    @Transient
    private Date expectedDeliveryDate;

    public PurchaseOrder(Date voucherDate, Integer year, Integer term, SlEntity vendor, BigDecimal amount, User budgetCheckedBy, User checkedBy, ArrayList<PoDetailDto> poDetails, String deliveryTerm, String deliveryAddress, Integer paymentTerm, String purpose, Vehicle vehicle, CashAdvance cashAdvance, Boolean useCreditCard, Department department, BudgetLineItemDetail budgetLineItemDetail, BigDecimal budgetLineItemBalancePOJORFP, BigDecimal budgetLineItemBalanceCV, BigDecimal cashFlowItemBalancePOJORFP, BigDecimal cashFlowItemBalanceCV, BigDecimal cashFlowItemTotal, String deliveryTimeAndCompletion) {
        this.voucherDate = voucherDate;
        this.year = year;
        this.term = term;
        this.vendor = vendor;
        this.amount = amount;
        this.budgetCheckedBy = budgetCheckedBy;
        this.checkedBy = checkedBy;
        this.poDetails = poDetails;
        this.deliveryTerm = deliveryTerm;
        this.deliveryAddress = deliveryAddress;
        this.paymentTerm = paymentTerm;
        this.purpose = purpose;
        this.vehicle = vehicle;
        this.cashAdvance = cashAdvance;
        this.useCreditCard = useCreditCard;
        this.department = department;
        this.budgetLineItemDetail = budgetLineItemDetail;
        this.budgetLineItemBalancePOJORFP = budgetLineItemBalancePOJORFP;
        this.budgetLineItemBalanceCV = budgetLineItemBalanceCV;
        this.cashFlowItemBalancePOJORFP = cashFlowItemBalancePOJORFP;
        this.cashFlowItemBalanceCV = cashFlowItemBalanceCV;
        this.cashFlowItemTotal = cashFlowItemTotal;
        this.deliveryTimeAndCompletion = deliveryTimeAndCompletion;
    }

}