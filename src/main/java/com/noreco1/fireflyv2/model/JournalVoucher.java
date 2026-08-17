package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class JournalVoucher extends Voucher {

    @NotNull(message = "Please enter explanation.")
    @Column
    private String explanation;

    @Column
    private String remarks;

    @NotNull(message = "Please select recommending officer.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_recommendedByUserId")
    private User recommendingOfficer;

    @NotNull(message = "Please select recommending officer.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_auditedByUserId")
    private User auditingOfficer;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_budgetedByUserId")
    private User budgetOfficer;

    @Transient
    private Integer tempBatchId;

    @Column
    private Boolean payable;

    @Column
    private Integer invDocTransactionId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_cashAdvanceLiquidationId", nullable = true, columnDefinition = "0")
    private CashAdvanceLiquidation cashAdvanceLiquidation;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_creditCardPurchaseRequestBatchId")
    private CreditCardPurchaseRequestBatch batch;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<BudgetLineItemDetail> budgetLineItemDetails = new ArrayList<>();

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<BudgetSubItem> budgetSubItems = new ArrayList<>();

}
