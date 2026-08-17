package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "MCDetails")
public class IomasMCDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MCd_Id")
    private Integer id;

    @Column(name = "MCh_Id")
    private Integer mchId;

    @Column(name = "Mat_Id", insertable = false, updatable = false)
    private Integer matId;

    @Column(name = "MCd_Qty")
    private Integer quantity;

    @Column(name = "MCd_AveCost")
    private BigDecimal averageCost;

    @Column(name = "MCd_Total")
    private BigDecimal total;

    @Column(name = "MCd_UnitCost")
    private BigDecimal unitCost;

    @Column(name = "MCd_TotalCost")
    private BigDecimal totalCost;

    public IomasMCDetail(Integer mchId, Integer matId, Integer quantity, BigDecimal averageCost, BigDecimal total,
                         BigDecimal unitCost, BigDecimal totalCost) {
        this.mchId = mchId;
        this.matId = matId;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.total = total;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
    }

}
