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
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class CheckVoucher extends Voucher implements Serializable {

    @NotNull(message = "Please enter particulars.")
    @Column
    private String particulars;

    @Column
    private String remarks;

    @Column
    private BigDecimal checkAmount = BigDecimal.ZERO;

    @NotNull(message = "Please select payee.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_payeeAccountNo")
    private SlEntity payee;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_budgetedByUserId")
    private User budgetOfficer;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_recommendedByUserId")
    private User recommendingOfficer;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_auditedByUserId")
    private User auditingOfficer;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_checkPrintedByUserId")
    private User checkPrinter;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_secondSignUserId")
    private User secondCheckSign;

    @Transient
    private List<CheckVoucherCheque> checkNumbers = new ArrayList<>();

    @Column
    private String rrNumber;

    @Transient
    private CashAdvance cashAdvance = new CashAdvance();

    @Column
    private String additionalPayeeInfo = "";

    @Transient
    private AccountsPayableVoucher accountsPayableVoucher = new AccountsPayableVoucher();

    @Transient
    private JournalVoucher journalVoucher = new JournalVoucher();

    @Transient
    private ReceivingReport receivingReport = new ReceivingReport();

    @Transient
    private JoAcceptance joAcceptance = new JoAcceptance();

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_bankId")
    private Bank bank;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_budgetDetailId")
    private BudgetDetail budgetDetail;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_budgetLineItemDetailId")
    private BudgetLineItemDetail budgetLineItemDetail;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_purchaseOrderId")
    private PurchaseOrder purchaseOrder;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_jobOrderId")
    private JobOrder jobOrder;

    @Transient
    private List<CashAdvance> cashAdvances = new ArrayList<>();

    @Transient
    private List<IEMOPBilling> iemopBillings = new ArrayList<>();

    @Transient
    private ArrayList<BudgetSubItem> budgetDetails = new ArrayList<>();

    @Transient
    private List<AccountsPayableVoucherInstallmentDetail> selectedInstallmentDetails = new ArrayList<>();

    @Transient
    private BankAccount bankAccount;

}
