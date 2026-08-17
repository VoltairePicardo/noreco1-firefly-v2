package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class QuotationDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_quotationId")
    private Quotation quotation;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_purchaseRequestDetailId")
    private PurchaseRequestDetail purchaseRequestDetail;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_supplierId")
    private Supplier supplier;

    @Column
    private BigDecimal price = BigDecimal.ZERO;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_awardedToSupplierId")
    private Supplier awardedToSupplier;

    @Transient
    private Boolean awardedToSupplier1;

    @Transient
    private BigDecimal priceSupplier1;

    @Transient
    private Boolean awardedToSupplier2;

    @Transient
    private BigDecimal priceSupplier2;

    @Transient
    private Boolean awardedToSupplier3;

    @Transient
    private BigDecimal priceSupplier3;

    @Transient
    private Boolean awardedToSupplier4;

    @Transient
    private BigDecimal priceSupplier4;

    @Column
    private Boolean isAvailable;

    public QuotationDetail(Quotation quotation, PurchaseRequestDetail purchaseRequestDetail, Supplier supplier, BigDecimal price, BigDecimal priceSupplier1, BigDecimal priceSupplier2, BigDecimal priceSupplier3, BigDecimal priceSupplier4, Boolean isAvailable) {
        this.quotation = quotation;
        this.purchaseRequestDetail = purchaseRequestDetail;
        this.supplier = supplier;
        this.price = price;
        this.priceSupplier1 = priceSupplier1;
        this.priceSupplier2 = priceSupplier2;
        this.priceSupplier3 = priceSupplier3;
        this.priceSupplier4 = priceSupplier4;
        this.isAvailable = isAvailable;
    }

    public QuotationDetail(Quotation quotation, PurchaseRequestDetail purchaseRequestDetail, Supplier supplier, BigDecimal price, Supplier awardedToSupplier, Boolean awardedToSupplier1, BigDecimal priceSupplier1, Boolean awardedToSupplier2, BigDecimal priceSupplier2, Boolean awardedToSupplier3, BigDecimal priceSupplier3, Boolean awardedToSupplier4, BigDecimal priceSupplier4, Boolean isAvailable) {
        this.quotation = quotation;
        this.purchaseRequestDetail = purchaseRequestDetail;
        this.supplier = supplier;
        this.price = price;
        this.awardedToSupplier = awardedToSupplier;
        this.awardedToSupplier1 = awardedToSupplier1;
        this.priceSupplier1 = priceSupplier1;
        this.awardedToSupplier2 = awardedToSupplier2;
        this.priceSupplier2 = priceSupplier2;
        this.awardedToSupplier3 = awardedToSupplier3;
        this.priceSupplier3 = priceSupplier3;
        this.awardedToSupplier4 = awardedToSupplier4;
        this.priceSupplier4 = priceSupplier4;
        this.isAvailable = isAvailable;
    }

}
