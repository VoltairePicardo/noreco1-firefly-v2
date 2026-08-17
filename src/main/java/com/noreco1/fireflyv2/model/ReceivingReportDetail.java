package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingReportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_receivingReportId")
    private ReceivingReport receivingReport;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_poDetailId")
    private PoDetail poDetail;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_itemTransactionDetailId")
    private ItemTransactionDetail itemTransactionDetail;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_purchaseRequestDetailId")
    private PurchaseRequestDetail purchaseRequestDetail;

    @ManyToOne
    @JoinColumn(name = "FK_itemId")
    private Item item;

    @Column
    private String deliveryNumber;

    @Column
    private BigDecimal quantityOrdered;

    @Column
    private BigDecimal quantityReceived;

    @Column
    private BigDecimal unitPrice;

    @Column
    private BigDecimal amount;

    @Column
    private BigDecimal discount;

    @Column
    private BigDecimal vat;

    @Column
    private BigDecimal adjustment;

    @Column
    private BigDecimal netAmount;

    @Column
    private BigDecimal netVatUnitPrice;

    @Column
    private BigDecimal netVatAmount;

    @Column
    private BigDecimal deliveredQuantity;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_joDetailId")
    private JoDetail joDetail;

    public ReceivingReportDetail(ReceivingReport receivingReport, PoDetail poDetail, ItemTransactionDetail itemTransactionDetail,
                                 PurchaseRequestDetail purchaseRequestDetail, Item item, String deliveryNumber, BigDecimal quantityOrdered,
                                 BigDecimal quantityReceived, BigDecimal unitPrice, BigDecimal amount, BigDecimal discount,
                                 BigDecimal vat, BigDecimal adjustment, BigDecimal netAmount, BigDecimal netVatUnitPrice,
                                 BigDecimal netVatAmount, BigDecimal deliveredQuantity, JoDetail joDetail) {
        this.receivingReport = receivingReport;
        this.poDetail = poDetail;
        this.itemTransactionDetail = itemTransactionDetail;
        this.purchaseRequestDetail = purchaseRequestDetail;
        this.item = item;
        this.deliveryNumber = deliveryNumber;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = quantityReceived;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.discount = discount;
        this.vat = vat;
        this.adjustment = adjustment;
        this.netAmount = netAmount;
        this.netVatUnitPrice = netVatUnitPrice;
        this.netVatAmount = netVatAmount;
        this.deliveredQuantity = deliveredQuantity;
        this.joDetail = joDetail;
    }

}
