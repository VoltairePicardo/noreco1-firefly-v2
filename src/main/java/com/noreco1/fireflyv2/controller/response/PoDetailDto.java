package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Brand;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Created by Personal on 5/15/2015.
 */
@Setter
@Getter
public class PoDetailDto {
    private Integer id;
    private Integer purchaseOrderId;
    private Integer itemId;
    private Integer rvDetailId;
    private String itemCode;
    private String unitCode;
    private String itemDescription;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal vat = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal itemAmount = BigDecimal.ZERO;
    private BigDecimal rvdQuantity = BigDecimal.ZERO;
    private BigDecimal remainingQuantity = BigDecimal.ZERO;
    private BigDecimal poQuantity = BigDecimal.ZERO;
    private String requisitionVoucherCode;
    private Brand brand;
    private BigDecimal sentForTestingQuantity;
    private BigDecimal deliveredQuantity;

    public PoDetailDto(Integer id, Integer purchaseOrderId, Integer itemId, Integer rvDetailId, String itemCode,
                       String unitCode, String itemDescription, BigDecimal quantity, BigDecimal vat, BigDecimal discount,
                       BigDecimal unitPrice, BigDecimal itemAmount, BigDecimal rvdQuantity, BigDecimal remainingQuantity,
                       BigDecimal poQuantity, String requisitionVoucherCode, Brand brand, BigDecimal sentForTestingQuantity, BigDecimal deliveredQuantity) {
        this.id = id;
        this.purchaseOrderId = purchaseOrderId;
        this.itemId = itemId;
        this.rvDetailId = rvDetailId;
        this.itemCode = itemCode;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.vat = vat;
        this.discount = discount;
        this.unitPrice = unitPrice;
        this.itemAmount = itemAmount;
        this.rvdQuantity = rvdQuantity;
        this.remainingQuantity = remainingQuantity;
        this.poQuantity = poQuantity;
        this.brand = brand;
        this.requisitionVoucherCode = requisitionVoucherCode;
        this.sentForTestingQuantity = sentForTestingQuantity;
        this.deliveredQuantity = deliveredQuantity;
    }

    public PoDetailDto() {}

}