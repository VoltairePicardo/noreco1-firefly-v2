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
public class PurchaseRequestDetail implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private BigDecimal quantity;

    @Column
    private BigDecimal poQuantity;

    @Column
    private BigDecimal withdrawQuantity = BigDecimal.ZERO;

    @Column
    private BigDecimal rrQuantity = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_itemId")
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="FK_unitId")
    private UnitMeasure unitMeasure;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_PurchaseRequestId")
    private PurchaseRequest purchaseRequest;

    @Column
    private String joDescription;

    public PurchaseRequestDetail(Integer id) {
        this.id = id;
    }

    public PurchaseRequestDetail(BigDecimal quantity, BigDecimal poQuantity, BigDecimal withdrawQuantity, BigDecimal rrQuantity, Item item, UnitMeasure unitMeasure, PurchaseRequest purchaseRequest, String joDescription) {
        this.quantity = quantity;
        this.poQuantity = poQuantity;
        this.withdrawQuantity = withdrawQuantity;
        this.rrQuantity = rrQuantity;
        this.item = item;
        this.unitMeasure = unitMeasure;
        this.purchaseRequest = purchaseRequest;
        this.joDescription = joDescription;
    }

}
