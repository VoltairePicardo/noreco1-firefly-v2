package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "SADetail")
public class IomasSOADetail {

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

    @Column(name = "d_UnitCost")
    private BigDecimal unitCost;

    @Column(name = "d_Total")
    private BigDecimal total;

    public IomasSOADetail(Integer hId, Integer matId, Integer quantity, BigDecimal unitCost, BigDecimal total) {
        this.hId = hId;
        this.matId = matId;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.total = total;
    }

}
