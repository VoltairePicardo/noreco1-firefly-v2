package com.noreco1.fireflyv2.controller.response.reports;

/**
 * Created by TSI Admin on 5/15/2015.
 */
public class CheckDto {
    private String checkNumber;
    private String date;
    private String payee;
    private String numericAmount;
    private String alphaAmount;
    private String sig1;
    private String sig2;

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getNumericAmount() {
        return numericAmount;
    }

    public void setNumericAmount(String numericAmount) {
        this.numericAmount = numericAmount;
    }

    public String getAlphaAmount() {
        return alphaAmount;
    }

    public void setAlphaAmount(String alphaAmount) {
        this.alphaAmount = alphaAmount;
    }

    public String getSig1() {
        return sig1;
    }

    public void setSig1(String sig1) {
        this.sig1 = sig1;
    }

    public String getSig2() {
        return sig2;
    }

    public void setSig2(String sig2) {
        this.sig2 = sig2;
    }
}
