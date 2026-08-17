package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "SADetails")
public class IomasSADetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SAd_Id")
    private Integer id;

    @Column(name = "SAh_Id")
    private Integer sahId;

    @Column(name = "Mat_Id", insertable = false, updatable = false)
    private Integer matId;

    @Column(name = "SAd_Qty")
    private Integer quantity;

    public IomasSADetail(Integer sahId, Integer matId, Integer quantity) {
        this.sahId = sahId;
        this.matId = matId;
        this.quantity = quantity;
    }

}
