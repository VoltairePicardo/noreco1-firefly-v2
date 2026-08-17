package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "MSDetails")
public class IomasMSDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MSd_Id")
    private Integer id;

    @Column(name = "MSh_Id")
    private Integer mshId;

    @Column(name = "Mat_Id", insertable = false, updatable = false)
    private Integer matId;

    @Column(name = "MSd_Qty")
    private Integer quantity;

    public IomasMSDetail(Integer mshId, Integer matId, Integer quantity) {
        this.mshId = mshId;
        this.matId = matId;
        this.quantity = quantity;
    }

}
