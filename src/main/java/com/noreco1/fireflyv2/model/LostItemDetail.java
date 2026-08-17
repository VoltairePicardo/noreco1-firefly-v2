package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class LostItemDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Integer matId;

    @Column
    private Integer quantity;

    @Column
    private BigDecimal aveCost;

    @Column
    private BigDecimal total;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_lostItemId", nullable = true, columnDefinition = "0")
    private LostItem lostItem;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
    private Item item;

    public LostItemDetail(Integer matId, Integer quantity, BigDecimal aveCost, BigDecimal total,
                          LostItem lostItem, Item item) {
        this.matId = matId;
        this.quantity = quantity;
        this.aveCost = aveCost;
        this.total = total;
        this.lostItem = lostItem;
        this.item = item;
    }

}
