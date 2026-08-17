package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class NeaPriceIndexDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_neaPriceIndexId")
    private NeaPriceIndex neaPriceIndex;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_itemId")
    private Item item;

    @Column
    private BigDecimal price;

    public NeaPriceIndexDetail(NeaPriceIndex neaPriceIndex, Item item, BigDecimal price) {
        this.neaPriceIndex = neaPriceIndex;
        this.item = item;
        this.price = price;
    }

}
