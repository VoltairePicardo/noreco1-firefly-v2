package com.noreco1.fireflyv2.model;

import com.noreco1.fireflyv2.controller.response.QuotationItemDto;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
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
public class Quotation extends Document implements Serializable {

    @Column
    private Date date;

    @Column
    private String particular;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_purchaseRequestId")
    private PurchaseRequest purchaseRequest;

    @Column
    private Integer year;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<QuotationItemDto> quotationDetails = new ArrayList<>();

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<Supplier> suppliers = new ArrayList<>();

    @Transient
    private boolean hasSupplier1;

    @Transient
    private boolean hasSupplier2;

    @Transient
    private boolean hasSupplier3;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<QuotationTerm> terms = new ArrayList<>();

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_approvedByGeneralManagerUserId")
    private User approvedByGeneralManager;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_approvedByFinanceManagerUserId")
    private User approvedByFinanceManager;
}
