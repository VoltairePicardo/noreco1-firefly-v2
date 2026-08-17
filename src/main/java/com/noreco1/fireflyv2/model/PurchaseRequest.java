package com.noreco1.fireflyv2.model;

import com.noreco1.fireflyv2.controller.response.RvDetailDto;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest extends Document implements Serializable {

    /*@NotNull(message = "Please enter voucher date.")*/
    @Column
    private Date voucherDate;

    /*@NotNull(message = "Please enter purpose.")*/
    @Column
    private String purpose;

    @Column
    private Date deliveryDate;

    @Column
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_employeeAccountNo")
    private SlEntity employee;

    @Column
    private Integer rvType;

    @Column
    private Date durationStart;

    @Column
    private Date durationEnd;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<RvDetailDto> rvDetails = new ArrayList<>();

    @Column
    private String rvItType;

    @Column
    private Date bacDate;
    @Column
    private BigDecimal estimatedAmount;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_budgetLineItemDetailId")
    private BudgetLineItemDetail budgetLineItemDetail;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_budgetSubItemId")
    private BudgetSubItem budgetSubItem;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_inventoryCheckedByUserId")
    private User inventoryCheckedBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_reviewedAcceptedByUserId")
    private User reviewedAcceptedBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_modeOfProcurementId")
    private ModeOfProcurement modeOfProcurement;

    @Column
    private Boolean emergencyPurchase;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_workOrderId")
    private WorkOrder workOrder;

    @NotFound(action=NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_vehicleId")
    private Vehicle vehicle;

    @NotFound(action=NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_departmentId")
    private Department department;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_costEstimateId")
    private CostEstimate costEstimate;

    public PurchaseRequest(Date voucherDate, String purpose, Date deliveryDate, Integer rvType,
                           Integer year, ArrayList<RvDetailDto> rvDetails,
                           String rvItType, SlEntity employee, Date durationStart,
                           Date durationEnd, Date bacDate, BigDecimal estimatedAmount,
                           BudgetLineItemDetail budgetLineItemDetail, BudgetSubItem budgetSubItem, User budgetCheckedBy,
                           User reviewedAcceptedBy, ModeOfProcurement modeOfProcurement,
                           Boolean emergencyPurchase, User inventoryCheckedBy, WorkOrder workOrder,
                           Vehicle vehicle, Department department) {
        this.voucherDate = voucherDate;
        this.purpose = purpose;
        this.deliveryDate = deliveryDate;
        this.rvType = rvType;
        this.year = year;
        this.rvDetails = rvDetails;
        this.rvItType = rvItType;
        this.setEmployee(employee);
        this.durationStart = durationStart;
        this.durationEnd = durationEnd;
        this.bacDate = bacDate;
        this.estimatedAmount = estimatedAmount;
        this.budgetLineItemDetail = budgetLineItemDetail;
        this.budgetSubItem = budgetSubItem;
        this.reviewedAcceptedBy = reviewedAcceptedBy;
        this.modeOfProcurement = modeOfProcurement;
        this.emergencyPurchase = emergencyPurchase;
        this.inventoryCheckedBy = inventoryCheckedBy;
        this.workOrder = workOrder;
        this.vehicle = vehicle;
        this.department = department;
    }

}
