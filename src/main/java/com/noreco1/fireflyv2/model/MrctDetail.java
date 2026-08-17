package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class MrctDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Integer mrhId;

    @Column
    private Integer mrdId;

    @Column
    private Integer matId;

    @Column
    private Integer quantity;

    @Column
    private BigDecimal averageCost;

    @Column
    private BigDecimal total;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_mrctId", nullable = true, columnDefinition = "0")
    private Mrct mrct;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
    private Item item;

    public MrctDetail(Integer mrhId, Integer mrdId, Integer matId, Integer quantity, BigDecimal averageCost,
                      BigDecimal total, Mrct mrct, Item item) {
        this.mrhId = mrhId;
        this.mrdId = mrdId;
        this.matId = matId;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.total = total;
        this.mrct = mrct;
        this.item = item;
    }

}
