package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "JRDetails")
public class IomasJRDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "d_Id")
    private Integer id;

    @Column(name = "h_Id")
    private Integer hId;

    @Column(name = "Mat_Id", insertable = false, updatable = false)
    private Integer matId;

    @Column(name = "d_Qty")
    private Integer quantity;

    @Column(name = "d_AveCost")
    private BigDecimal averageCost;

    @Column(name = "d_Total")
    private BigDecimal total;

    @Column(name = "d_Reference")
    private String reference;

    @Column(name = "d_SalesCost")
    private BigDecimal salesCost;

    @Column(name = "d_SalesTotal")
    private BigDecimal salesTotal;

    public IomasJRDetail(Integer hId, Integer matId, Integer quantity, BigDecimal averageCost, BigDecimal total,
                         String reference, BigDecimal salesCost, BigDecimal salesTotal) {
        this.hId = hId;
        this.matId = matId;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.total = total;
        this.reference = reference;
        this.salesCost = salesCost;
        this.salesTotal = salesTotal;
    }

}