package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "HWIDetails")
public class IomasHWIDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MRd_Id")
    private Integer id;

    @Column(name = "MRh_Id")
    private Integer mrhId;

    @Column(name = "Mat_Id", insertable = false, updatable = false)
    private Integer matId;

    @Column(name = "MRd_Qty")
    private Integer quantity;

    @Column(name = "MRd_AveCost")
    private BigDecimal aveCost;

    @Column(name = "MRd_Total")
    private BigDecimal total;

    @Column(name = "MRd_Reference")
    private String reference;

    public IomasHWIDetail(Integer mrhId, Integer matId, Integer quantity, BigDecimal aveCost, BigDecimal total, String reference) {
        this.mrhId = mrhId;
        this.matId = matId;
        this.quantity = quantity;
        this.aveCost = aveCost;
        this.total = total;
        this.reference = reference;
    }

}
