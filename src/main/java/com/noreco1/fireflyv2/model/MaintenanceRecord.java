package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecord implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String code;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_assetId")
    private Asset asset;

    @Column
    private Date date = new Date();

    @Column
    private BigDecimal odometerReading;

    @Column
    private Date nextPmsDate;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_voucherTransactionId")
    private Transaction voucherTransaction;

    @Column
    private Date maintenanceDate = new Date();

    @Transient
    private List<MaintenanceRecordWork> maintenanceRecordWorks = new ArrayList<>();

    @Transient
    private List<MaintenanceRecordOtherItem> maintenanceRecordOtherItems = new ArrayList<>();

    @Transient
    private List<MaintenanceRecordMaterialRelease> maintenanceRecordMaterialReleases = new ArrayList<>();

    @Transient
    private List<StockTransactionDetail> stockTransactionDetails = new ArrayList<>();

    @Transient
    private List<Map> excludedStockReleaseItems  = new ArrayList<>();

    public MaintenanceRecord(Asset asset, String code, Date date, BigDecimal odometerReading, Date nextPmsDate,
                             List<MaintenanceRecordWork> maintenanceRecordWorks, Transaction voucherTransaction,
                             List<MaintenanceRecordMaterialRelease> maintenanceRecordMaterialReleases,
                             List<MaintenanceRecordOtherItem> maintenanceRecordOtherItems, Date maintenanceDate,
                             List<Map> excludedStockReleaseItems) {
        this.asset = asset;
        this.code = code;
        this.date = date;
        this.odometerReading = odometerReading;
        this.nextPmsDate = nextPmsDate;
        this.maintenanceRecordWorks = maintenanceRecordWorks;
        this.voucherTransaction = voucherTransaction;
        this.maintenanceRecordMaterialReleases = maintenanceRecordMaterialReleases;
        this.maintenanceRecordOtherItems = maintenanceRecordOtherItems;
        this.maintenanceDate = maintenanceDate;
        this.excludedStockReleaseItems = excludedStockReleaseItems;
    }

    public String getItems() {

        StringBuilder items = new StringBuilder();
        DecimalFormat formatter = new DecimalFormat("#,##0.00");

        if (!this.stockTransactionDetails.isEmpty()) {

            for (StockTransactionDetail stockTransactionDetail : this.stockTransactionDetails) {
                items.append(this.stockTransactionDetails.size() > 1 ? " * " : "").append(stockTransactionDetail.getItemStock().getItem().getDescription()).append(" - ").append(formatter.format(stockTransactionDetail.getTotalCost())).append("\n");
            }

            return items.substring(0, items.length() - 1);
        }

        return items.toString();

    }
    public String getWorks() {

        StringBuilder works = new StringBuilder();
        DecimalFormat formatter = new DecimalFormat("#,##0.00");

        if (!this.maintenanceRecordWorks.isEmpty()) {

            for (MaintenanceRecordWork maintenanceRecordWork: this.maintenanceRecordWorks) {
                works.append(this.maintenanceRecordWorks.size() > 1 ? " * " : "").append(maintenanceRecordWork.getDescription()).append(" - ").append(formatter.format(maintenanceRecordWork.getAmount())).append("\n");
            }

            return works.substring(0, works.length() - 1);
        }

        return works.toString();
    }

}
