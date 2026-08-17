package com.noreco1.fireflyv2.model;

import com.noreco1.fireflyv2.controller.response.JoDetailDto;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class JobOrder extends Document implements Serializable {

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
    @JoinColumn(name = "FK_notedByUserId")
    private User notedBy;

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
    private ArrayList<JoDetailDto> joDetails = new ArrayList<>();

    @Column
    private String description;

    @Column
    private Integer paymentTerm;

    @Column
    private String paymentTermInWords;

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
    private ArrayList<JobOrderBudgetDetail> budgetDetails = new ArrayList<>();
}
