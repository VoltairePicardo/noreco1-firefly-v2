package com.noreco1.fireflyv2.model;

import com.noreco1.fireflyv2.controller.response.EmployeeDto;
import com.noreco1.fireflyv2.controller.response.StockWithdrawalDetailDto;
import com.noreco1.fireflyv2.controller.response.TurnOnOrderWithdrawalDto;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class StockWithdrawal extends Document implements Serializable {

    @Column
    private String description;

    @Column
    private Date voucherDate;

    @Column
    private Integer year;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_departmentId", nullable = true, columnDefinition = "0")
    private Department department;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_inventoryLocationId", nullable = true, columnDefinition = "0")
    private InventoryLocation inventoryLocation;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_inventoryCategoryId", nullable = true, columnDefinition = "0")
    private InventoryCategory inventoryCategory;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_workOrderId", nullable = true, columnDefinition = "0")
    private WorkOrder workOrder;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_costEstimateId", nullable = true, columnDefinition = "0")
    private CostEstimate costEstimate;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<StockWithdrawalDetailDto> details = new ArrayList<>();

    @Column
    private Integer type;

    @Column(name="FK_turnOnOrderWithdrawalId")
    private Integer turnOnOrderWithdrawalId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_purchaseRequestId", nullable = true, columnDefinition = "0")
    private PurchaseRequest purchaseRequest;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_purposeId", nullable = true, columnDefinition = "0")
    private Purpose purpose;

    @Transient
    private TurnOnOrderWithdrawalDto turnOnOrderWithdrawal;

    @Transient
    private List<EmployeeDto> employees = new ArrayList<>();


}
