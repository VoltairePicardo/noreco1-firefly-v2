package com.noreco1.fireflyv2.model.form;

public class YearMonth {

    private Integer month = 0;
    private Integer year = 0;

    public YearMonth() {}

    public YearMonth(Integer month, Integer year) {
        this.month = month;
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
