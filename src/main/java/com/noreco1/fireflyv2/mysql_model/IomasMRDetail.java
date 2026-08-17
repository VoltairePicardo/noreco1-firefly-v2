package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import com.noreco1.fireflyv2.model.Item;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "MRDetails")
public class IomasMRDetail {

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
    private BigDecimal averageCost;

    @Column(name = "MRd_Total")
    private BigDecimal total;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="Mat_Id", nullable = true, columnDefinition = "0")
    private IomasMaterial item;

    public IomasMRDetail(Integer mrhId, Integer matId, Integer quantity, BigDecimal averageCost, BigDecimal total, IomasMaterial item) {
        this.mrhId = mrhId;
        this.matId = matId;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.total = total;
        this.item = item;
    }

}
