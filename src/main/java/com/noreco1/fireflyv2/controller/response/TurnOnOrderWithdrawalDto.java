package com.noreco1.fireflyv2.controller.response;

import java.util.Date;

/**
 * Created by Tri-Nvent on 7/15/2020.
 */
public class TurnOnOrderWithdrawalDto {

    Integer id;
    Date date;
    Integer totalTurnOnOrders;

    public TurnOnOrderWithdrawalDto(){}

    public TurnOnOrderWithdrawalDto(Integer id, Date date, Integer totalTurnOnOrders) {
        this.id = id;
        this.date = date;
        this.totalTurnOnOrders = totalTurnOnOrders;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getTotalTurnOnOrders() {
        return totalTurnOnOrders;
    }

    public void setTotalTurnOnOrders(Integer totalTurnOnOrders) {
        this.totalTurnOnOrders = totalTurnOnOrders;
    }
}
