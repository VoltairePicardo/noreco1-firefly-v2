package com.noreco1.fireflyv2.model;

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
public class PaymentRequest extends DocumentNoApproval implements Serializable {

    @NotNull(message = "Please enter voucher date.")
    @Column
    private Date voucherDate;

    @Column
    private Integer year;

    @NotNull(message = "Please select vendor.")
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="FK_vendorAccountNo")
    private SlEntity vendor;

    @NotNull(message = "Total Amount must be greater than zero.")
    private BigDecimal amount = BigDecimal.ZERO;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<PaymentRequestDetail> paymentRequestDetails = new ArrayList<>();

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date invoiceDate;

    @Column
    private String invoiceNumber;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dueDate;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<PaymentRequestBudgetDetail> budgetDetails = new ArrayList<>();

    @Column
    private BigDecimal cashFlowAmountBalancePOJORFP = BigDecimal.ZERO;

    @Column
    private BigDecimal cashFlowAmountBalanceCV = BigDecimal.ZERO;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<BudgetLineItemDetail> budgetLineItemDetails = new ArrayList<>();

}