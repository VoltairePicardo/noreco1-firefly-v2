package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.controller.response.GeneralLedgerLineDto;
import com.noreco1.fireflyv2.controller.response.PrepaymentDetailDto;
import com.noreco1.fireflyv2.controller.response.SubLedgerDto;
import com.noreco1.fireflyv2.controller.response.TemporaryBatchDto;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Prepayment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String code;

    @Column
    private Integer year;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_transactionId")
    private Transaction transaction;

    @NotNull(message = "Please enter date paid1.")
    @Column
    private Date datePaid;

    @NotNull(message = "Failed to insert accountNo.")
    @Column(name = "FK_accountNo")
    private Integer accountNo;

    @NotNull(message = "Please enter description.")
    @Column
    private String description;

    @NotNull(message = "Please select Prepayment Account.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="FK_prepaymentAccountId")
    private Account prepaymentAccount;

    @NotNull(message = "Please select Expense Account.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="FK_expenseAccountId")
    private Account expenseAccount;

    @NotNull(message = "Total Cost must be greater than zero.")
    @Column
    private BigDecimal totalCost = BigDecimal.ZERO;

    @NotNull(message = "Please enter no. of months.")
    @Column
    private Integer noOfMonths;

    @NotNull(message = "Monthly Cost must be greater than zero.")
    @Column
    private BigDecimal monthlyCost = BigDecimal.ZERO;

    @NotNull(message = "Applied Cost must be greater than zero.")
    @Column
    private BigDecimal appliedCost = BigDecimal.ZERO;

    @NotNull(message = "Balance must be greater than zero.")
    @Column
    private BigDecimal balance = BigDecimal.ZERO;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_createdByUserId", nullable = true, columnDefinition = "0")
    private User createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    private boolean hasPpd;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    private ArrayList<PrepaymentDetailDto> ppDetails = new ArrayList<>();

    @Transient
    private List<GeneralLedgerLineDto> tempGLs = new ArrayList<>();

    @Transient
    private List<SubLedgerDto> tempSLs = new ArrayList<>();

    @Transient
    private TemporaryBatchDto tempBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_SLEntityClassificationId", nullable = false)
    private SLEntityClassification slEntityClassification;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_officeId")
    private Office office;

    @Transient
    private BigDecimal prepaymentAmount = BigDecimal.ZERO;

    @Transient
    private BigDecimal expenseAmount = BigDecimal.ZERO;

    @Transient
    private Date processDate;

    @Column
    private Integer startMonth;

    @Column
    private Integer startYear;

    public Prepayment(String code, Integer year, Transaction transaction, Date datePaid, Integer accountNo,
                      String description, Account prepaymentAccount, Account expenseAccount, BigDecimal totalCost,
                      Integer noOfMonths, BigDecimal monthlyCost, BigDecimal appliedCost, BigDecimal balance,
                      User createdBy, Date createdAt, Date updatedAt, boolean hasPpd, ArrayList<PrepaymentDetailDto> ppDetails,
                      List<GeneralLedgerLineDto> tempGLs, List<SubLedgerDto> tempSLs, TemporaryBatchDto tempBatch,
                      SLEntityClassification slEntityClassification, Office office, BigDecimal prepaymentAmount,
                      BigDecimal expenseAmount, Date processDate, Integer startMonth, Integer startYear) {
        this.code = code;
        this.year = year;
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.transaction = transaction;
        this.datePaid = datePaid;
        this.accountNo = accountNo;
        this.description = description;
        this.prepaymentAccount = prepaymentAccount;
        this.expenseAccount = expenseAccount;
        this.totalCost = totalCost;
        this.noOfMonths = noOfMonths;
        this.monthlyCost = monthlyCost;
        this.appliedCost = appliedCost;
        this.balance = balance;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hasPpd = hasPpd;
        this.ppDetails = ppDetails;
        this.tempGLs = tempGLs;
        this.tempSLs = tempSLs;
        this.tempBatch = tempBatch;
        this.slEntityClassification = slEntityClassification;
        this.office = office;
        this.prepaymentAmount = prepaymentAmount;
        this.expenseAmount = expenseAmount;
        this.processDate = processDate;
    }

    public Prepayment(String code, Integer year, Transaction transaction, Date datePaid, Integer accountNo, String description, Account prepaymentAccount, Account expenseAccount, BigDecimal totalCost, Integer noOfMonths, BigDecimal monthlyCost, BigDecimal appliedCost, BigDecimal balance, User createdBy, Date createdAt, Date updatedAt, boolean hasPpd, ArrayList<PrepaymentDetailDto> ppDetails, SLEntityClassification slEntityClassification) {
        this.code = code;
        this.year = year;
        this.transaction = transaction;
        this.datePaid = datePaid;
        this.accountNo = accountNo;
        this.description = description;
        this.prepaymentAccount = prepaymentAccount;
        this.expenseAccount = expenseAccount;
        this.totalCost = totalCost;
        this.noOfMonths = noOfMonths;
        this.monthlyCost = monthlyCost;
        this.appliedCost = appliedCost;
        this.balance = balance;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hasPpd = hasPpd;
        this.ppDetails = ppDetails;
        this.slEntityClassification = slEntityClassification;
    }

    public Prepayment(String code, Transaction transaction, Date datePaid, Integer accountNo, String description, Account prepaymentAccount, Account expenseAccount, BigDecimal totalCost, Integer noOfMonths, BigDecimal monthlyCost, BigDecimal appliedCost, BigDecimal balance, User createdBy, Date createdAt, Date updatedAt, Integer year, SLEntityClassification slEntityClassification) {
        this.code = code;
        this.transaction = transaction;
        this.datePaid = datePaid;
        this.accountNo = accountNo;
        this.description = description;
        this.prepaymentAccount = prepaymentAccount;
        this.expenseAccount = expenseAccount;
        this.totalCost = totalCost;
        this.noOfMonths = noOfMonths;
        this.monthlyCost = monthlyCost;
        this.appliedCost = appliedCost;
        this.balance = balance;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.year = year;
        this.slEntityClassification = slEntityClassification;
    }

}