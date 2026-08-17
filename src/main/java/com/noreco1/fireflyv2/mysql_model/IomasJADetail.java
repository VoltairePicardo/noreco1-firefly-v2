package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "JADetails")
public class IomasJADetail {

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

    public IomasJADetail(Integer hId, Integer matId, Integer quantity) {
        this.hId = hId;
        this.matId = matId;
        this.quantity = quantity;
    }

}
