package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class QuotationItem implements Serializable {

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

    @Column
    private Boolean isAvailable;

    @Transient
    List<QuotationItem> quotationItems = new ArrayList<>();

    public QuotationItem(Quotation quotation, PurchaseRequestDetail purchaseRequestDetail, Boolean isAvailable, List<QuotationItem> quotationItems) {
        this.quotation = quotation;
        this.purchaseRequestDetail = purchaseRequestDetail;
        this.isAvailable = isAvailable;
        this.quotationItems = quotationItems;
    }

}
