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
public class PoDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_purchaseOrderId")
    private PurchaseOrder purchaseOrder;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_purchaseRequestDetailId")
    private PurchaseRequestDetail purchaseRequestDetail;

    @Column
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column
    private BigDecimal deliveredQuantity = BigDecimal.ZERO;

    @Column
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column
    private BigDecimal vat = BigDecimal.ZERO;

    @Column
    private BigDecimal discount = BigDecimal.ZERO;

    @Column
    private BigDecimal amount = BigDecimal.ZERO;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_brandId", updatable = false)
    private Brand brand;

    public PoDetail(PurchaseOrder purchaseOrder, PurchaseRequestDetail purchaseRequestDetail, BigDecimal quantity, BigDecimal deliveredQuantity,
                    BigDecimal unitPrice, BigDecimal vat, BigDecimal discount, BigDecimal amount, Brand brand) {
        this.purchaseOrder = purchaseOrder;
        this.purchaseRequestDetail = purchaseRequestDetail;
        this.quantity = quantity;
        this.deliveredQuantity = deliveredQuantity;
        this.unitPrice = unitPrice;
        this.vat = vat;
        this.discount = discount;
        this.amount = amount;
        this.brand = brand;
    }

}

