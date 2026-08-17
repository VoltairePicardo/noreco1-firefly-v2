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
public class QuotationItemDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_quotationItemId")
    private QuotationItem quotationItem;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_supplierId", updatable = false)
    private Supplier supplier;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_brandId", updatable = false)
    private Brand brand;

    @Column
    BigDecimal price;

    @Column
    private Boolean isAwarded;

    public QuotationItemDetail(QuotationItem quotationItem, Supplier supplier, Brand brand, BigDecimal price, Boolean isAwarded) {
        this.quotationItem = quotationItem;
        this.supplier = supplier;
        this.brand = brand;
        this.price = price;
        this.isAwarded = isAwarded;
    }

}
