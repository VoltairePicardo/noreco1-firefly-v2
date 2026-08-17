package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.controller.response.CostEstimateDetailDto;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
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
public class CostEstimate extends Document implements Serializable {
    @Column
    private Date voucherDate;

    @Column
    private Integer year;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_projectId", nullable = true, columnDefinition = "0")
    private Project project;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_inventoryLocationId", nullable = true, columnDefinition = "0")
    private InventoryLocation inventoryLocation;

    @Column
    private BigDecimal totalAssemblyLaborCost;

    @Column
    private BigDecimal totalMaterialCost;

    @Column
    private BigDecimal totalMeteringCost;

    @Column
    private BigDecimal totalMiscellaneousCharge;

    @Column
    private BigDecimal laborCost;

    @Column
    private BigDecimal freightHandling;

    @Column
    private String notes;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_checkedByUserId", nullable = true, columnDefinition = "0")
    private User checker;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_recommendedByUserId", nullable = true, columnDefinition = "0")
    private User recommendedBy;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<CostEstimateAssemblyUnit> costEstimateAssemblyUnits = new ArrayList<>();

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<CostEstimateDetailDto> details = new ArrayList<>();

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<CostEstimateMiscellaneousCharge> miscellaneousCharges = new ArrayList<>();

    @Column
    private BigDecimal laborCostPercentage = BigDecimal.ZERO;

    @Column
    private BigDecimal freightHandlingPercentage = BigDecimal.ZERO;

    @Column(name = "`type`")
    private int type;

    @Column
    private BigDecimal contingency;

    @Column
    private BigDecimal contingencyPercentage = BigDecimal.ZERO;

}
