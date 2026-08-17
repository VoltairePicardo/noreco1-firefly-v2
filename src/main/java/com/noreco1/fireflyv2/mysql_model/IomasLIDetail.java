package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "LIDetail")
public class IomasLIDetail {

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
    private BigDecimal aveCost;

    @Column(name = "d_Total")
    private BigDecimal total;

    @Column(name = "d_PARNo")
    private Integer PARNo;

    public IomasLIDetail(Integer hId, Integer matId, Integer quantity, BigDecimal aveCost, BigDecimal total, Integer PARNo) {
        this.hId = hId;
        this.matId = matId;
        this.quantity = quantity;
        this.aveCost = aveCost;
        this.total = total;
        this.PARNo = PARNo;
    }

}
