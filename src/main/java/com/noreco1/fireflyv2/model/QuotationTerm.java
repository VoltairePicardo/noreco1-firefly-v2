package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class QuotationTerm implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_quotationId", updatable = false)
    private Quotation quotation;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_supplierId", updatable = false)
    private Supplier supplier;

    @Column
    private String deliveryTimeAndCompletion;

    @Column
    private String warrantyPeriod;

    @Column
    private Integer termsOfPayment;

    @Column
    private String placeOfDelivery;

    public QuotationTerm(Quotation quotation, Supplier supplier, String deliveryTimeAndCompletion, String warrantyPeriod,
                         Integer termsOfPayment, String placeOfDelivery) {
        this.quotation = quotation;
        this.supplier = supplier;
        this.deliveryTimeAndCompletion = deliveryTimeAndCompletion;
        this.warrantyPeriod = warrantyPeriod;
        this.termsOfPayment = termsOfPayment;
        this.placeOfDelivery = placeOfDelivery;
    }

}
