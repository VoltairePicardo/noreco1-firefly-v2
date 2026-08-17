package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class AccountsPayableVoucher extends Voucher {

    @NotNull(message = "Please enter particulars.")
    @Column
    private String particulars;

    @NotNull(message = "Please enter due date.")
    @Column
    private Date dueDate;

    @NotNull(message = "Please select vendor.")
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_vendorAccountNo")
    private SlEntity vendor;

    @Column
    private Date invoiceDate;

    @Column
    private Integer paymentTerm;

    @Transient
    private Integer paymentRequestId;

    @Transient
    private Integer receivingReportId;

    @Transient
    private Integer joAcceptanceId;

    @Transient
    private List<ReceivingReport> receivingReports;

    @Column
    private String unpaidRemark;

    @Column
    private Boolean forInstallment;

    @Column
    private Integer numberOfPayments;

    @Transient
    private List<IEMOPBilling> iemopBillings = new ArrayList<>();

    @Transient
    private List<AccountsPayableVoucherInstallmentDetail> installmentDetails = new ArrayList<>();

}
