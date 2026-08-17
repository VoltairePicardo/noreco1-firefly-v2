package com.noreco1.fireflyv2.controller.response;

public class CreditCardPurchaseRequestBatchDto {

    private Integer id;
    private Integer status;

    public CreditCardPurchaseRequestBatchDto() {
    }

    public CreditCardPurchaseRequestBatchDto(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
