package com.noreco1.fireflyv2.controller.response;


import com.noreco1.fireflyv2.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Tri-Nvent on 3/3/2020.
 */
public class MaintenanceRecordDto {

    private Integer id;
    private String code;
    private Asset asset;
    private Transaction voucherTransaction;
    private Date maintenanceDate;
    private Date date;
    private BigDecimal odometerReading;
    private Date nextPmsDate;
    private VoucherDto voucher;
    private List<MaintenanceRecordWork> maintenanceRecordWorks;
    private List<MaintenanceRecordOtherItem> maintenanceRecordOtherItems;
    private List<MaintenanceRecordMaterialReleaseDto> maintenanceRecordMaterialReleases;
    private VehicleInformation vehicleInformation;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Transaction getVoucherTransaction() {
        return voucherTransaction;
    }

    public void setVoucherTransaction(Transaction voucherTransaction) {
        this.voucherTransaction = voucherTransaction;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public VoucherDto getVoucher() {
        return voucher;
    }

    public void setVoucher(VoucherDto voucher) {
        this.voucher = voucher;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<MaintenanceRecordWork> getMaintenanceRecordWorks() {
        return maintenanceRecordWorks;
    }

    public void setMaintenanceRecordWorks(List<MaintenanceRecordWork> maintenanceRecordWorks) {
        this.maintenanceRecordWorks = maintenanceRecordWorks;
    }

    public List<MaintenanceRecordMaterialReleaseDto> getMaintenanceRecordMaterialReleases() {
        return maintenanceRecordMaterialReleases;
    }

    public void setMaintenanceRecordMaterialReleases(List<MaintenanceRecordMaterialReleaseDto> maintenanceRecordMaterialReleases) {
        this.maintenanceRecordMaterialReleases = maintenanceRecordMaterialReleases;
    }

    public BigDecimal getOdometerReading() {
        return odometerReading;
    }

    public void setOdometerReading(BigDecimal odometerReading) {
        this.odometerReading = odometerReading;
    }

    public Date getNextPmsDate() {
        return nextPmsDate;
    }

    public void setNextPmsDate(Date nextPmsDate) {
        this.nextPmsDate = nextPmsDate;
    }

    public VehicleInformation getVehicleInformation() {
        return vehicleInformation;
    }

    public void setVehicleInformation(VehicleInformation vehicleInformation) {
        this.vehicleInformation = vehicleInformation;
    }

    public List<MaintenanceRecordOtherItem> getMaintenanceRecordOtherItems() {
        return maintenanceRecordOtherItems;
    }

    public void setMaintenanceRecordOtherItems(List<MaintenanceRecordOtherItem> maintenanceRecordOtherItems) {
        this.maintenanceRecordOtherItems = maintenanceRecordOtherItems;
    }

    public Date getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(Date maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }
}
