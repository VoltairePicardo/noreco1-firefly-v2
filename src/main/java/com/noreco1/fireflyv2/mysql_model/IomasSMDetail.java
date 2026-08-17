package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "SMDetails")
public class IomasSMDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SMd_Id")
    private Integer id;

    @Column(name = "SMh_Id")
    private Integer smhId;

    @Column(name = "Mat_Id", insertable = false, updatable = false)
    private Integer matId;

    @Column(name = "SMd_Qty")
    private Integer quantity;

    @Column(name = "smd_AveCost")
    private BigDecimal aveCost;

    @Column(name = "smd_Total")
    private BigDecimal total;

    @Column(name = "SMd_Reference")
    private String reference;

    @Column(name = "d_SalesCost")
    private BigDecimal salesCost;

    @Column(name = "d_SalesTotal")
    private BigDecimal salesTotal;

    public IomasSMDetail(Integer smhId, Integer matId, Integer quantity, BigDecimal aveCost, BigDecimal total,
                         String reference, BigDecimal salesCost, BigDecimal salesTotal) {
        this.smhId = smhId;
        this.matId = matId;
        this.quantity = quantity;
        this.aveCost = aveCost;
        this.total = total;
        this.reference = reference;
        this.salesCost = salesCost;
        this.salesTotal = salesTotal;
    }

}
