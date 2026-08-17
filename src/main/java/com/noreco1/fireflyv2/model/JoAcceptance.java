package com.noreco1.fireflyv2.model;

import com.noreco1.fireflyv2.controller.response.JoAcceptanceDetailDto;
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
public class JoAcceptance extends DocumentNoApproval implements Serializable {

    @NotNull(message = "Please enter voucher date.")
    @Column
    private Date voucherDate;

    @Column
    private Integer year;

    @NotNull(message = "Please select vendor.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_vendorAccountNo")
    private SlEntity vendor;

    @NotFound(action = NotFoundAction.IGNORE)
    @NotNull(message = "Please select Job Order.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_jobOrderId")
    private JobOrder jobOrder;

    @NotNull(message = "Total Amount must be greater than zero.")
    private BigDecimal amount = BigDecimal.ZERO;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_inspectedByUserId")
    private User inspectedBy;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<JoAcceptanceDetailDto> joAcceptanceDetails = new ArrayList<>();

    @Column
    private Boolean hasPayReq;

    @Column
    private Integer type;

    @Column
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column
    private BigDecimal adjustment = BigDecimal.ZERO;

    @Column
    private Boolean isFullPayment;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date invoiceDate;

    @Column
    private String invoiceNumber;

    public Boolean hasReqPay() {
        return hasPayReq;
    }

    

}