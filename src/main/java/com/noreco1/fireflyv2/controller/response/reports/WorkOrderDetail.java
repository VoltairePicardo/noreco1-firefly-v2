package com.noreco1.fireflyv2.controller.response.reports;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by lenovo on 2/20/2016.
 */
public class WorkOrderDetail {

    private String workOrderNo;
    private String projectDescription;
    private Date dateStarted;
    private Date targetCompletion;
    private BigDecimal amount;
    private BigDecimal materials;
    private BigDecimal labor;
    private BigDecimal houseConnections;
    private BigDecimal overhead;
    private BigDecimal totalPositive;
    private BigDecimal totalNegative;

    public WorkOrderDetail() {
    }

    public WorkOrderDetail(String workOrderNo, String projectDescription, Date dateStarted, Date targetCompletion, BigDecimal amount, BigDecimal materials, BigDecimal labor, BigDecimal houseConnections, BigDecimal overhead, BigDecimal totalPositive, BigDecimal totalNegative) {
        this.workOrderNo = workOrderNo;
        this.projectDescription = projectDescription;
        this.dateStarted = dateStarted;
        this.targetCompletion = targetCompletion;
        this.amount = amount;
        this.materials = materials;
        this.labor = labor;
        this.houseConnections = houseConnections;
        this.overhead = overhead;
        this.totalPositive = totalPositive;
        this.totalNegative = totalNegative;
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public Date getTargetCompletion() {
        return targetCompletion;
    }

    public void setTargetCompletion(Date targetCompletion) {
        this.targetCompletion = targetCompletion;
    }

    public String getAge() {

        Date today = new Date();

        long age = today.getTime() - this.dateStarted.getTime();

        return (age / (1000 * 60 * 60 * 24)) + " days old";
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getMaterials() {
        return materials;
    }

    public void setMaterials(BigDecimal materials) {
        this.materials = materials;
    }

    public BigDecimal getLabor() {
        return labor;
    }

    public void setLabor(BigDecimal labor) {
        this.labor = labor;
    }

    public BigDecimal getHouseConnections() {
        return houseConnections;
    }

    public void setHouseConnections(BigDecimal houseConnections) {
        this.houseConnections = houseConnections;
    }

    public BigDecimal getOverhead() {
        return overhead;
    }

    public void setOverhead(BigDecimal overhead) {
        this.overhead = overhead;
    }

    public BigDecimal getTotalPositive() {
        return totalPositive;
    }

    public void setTotalPositive(BigDecimal totalPositive) {
        this.totalPositive = totalPositive;
    }

    public BigDecimal getTotalNegative() {
        return totalNegative;
    }

    public void setTotalNegative(BigDecimal totalNegative) {
        this.totalNegative = totalNegative;
    }
}
