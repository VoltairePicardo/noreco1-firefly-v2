package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.ItemTransactionDetail;
import com.noreco1.fireflyv2.model.StockTransactionDetail;
import com.noreco1.fireflyv2.model.StockWithdrawalDetail;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Tri-Nvent on 3/5/2020.
 */
public class StockWithdrawalDto {

    private Integer id;
    private String code;
    private String description;
    private Date voucherDate;
    private Integer year;
    private Integer type;
    ArrayList<StockWithdrawalDetail> details = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public ArrayList<StockWithdrawalDetail> getDetails() {
        return details;
    }

    public void setDetails(ArrayList<StockWithdrawalDetail> details) {
        this.details = details;
    }
}
