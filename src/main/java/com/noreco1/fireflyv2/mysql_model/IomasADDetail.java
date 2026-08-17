package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "ADDetails")
public class IomasADDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ADd_Id")
    private Integer id;

    @Column(name = "ADh_Id")
    private Integer adhId;

    @Column(name = "Mat_Id")
    private Integer matId;

    @Column(name = "ADd_Qty")
    private Integer quantity;

    @Column(name = "ADd_AveCost")
    private BigDecimal averageCost;

    @Column(name = "ADd_Total")
    private BigDecimal total;

    public IomasADDetail(Integer adhId, Integer matId, Integer quantity, BigDecimal averageCost, BigDecimal total) {
        this.adhId = adhId;
        this.matId = matId;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.total = total;
    }

}
