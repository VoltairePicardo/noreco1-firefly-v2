package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class IEMOPBilling implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_subSupplierId")
    private SubSupplier subSupplier;

    @Column
    private String referenceNumber;

    @Column
    private Date transDate;

    @Column
    private String stlId;

    @Column
    private String billingId;

    @Column
    private String facilityType;

    @Column
    private Boolean whtAgent;

    @Column
    private Boolean ithTag;

    @Column
    private Boolean nonVatable;

    @Column
    private Boolean zeroRated;

    @Column
    private BigDecimal vatableSales;

    @Column
    private BigDecimal zeroRatedSales;

    @Column
    private BigDecimal zeroRatedEcoSales;

    @Column
    private BigDecimal vatOnSales;

    @Column
    private BigDecimal vatablePurchases;

    @Column
    private BigDecimal zeroRatedPurchases;

    @Column
    private BigDecimal zeroRatedEcoPurchases;

    @Column
    private BigDecimal vatOnPurchases;

    @Column
    private BigDecimal ewtSales;

    @Column
    private BigDecimal ewtPurchases;

    @Column
    private String remarks;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_createdByUserId")
    private User createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

}
