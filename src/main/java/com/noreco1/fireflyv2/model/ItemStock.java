package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ItemStock implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_itemId")
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_inventoryLocationId")
    private InventoryLocation inventoryLocation;

    @Column
    private BigDecimal quantity;//old column

    @Column
    private BigDecimal unitCost;//old column

    @Column
    private BigDecimal totalQuantity;//new column

    @Column
    private BigDecimal totalItemCost;//new column

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;

    @Transient
    private String base64Image = "";

    @Transient
    private BigDecimal balance;

    public ItemStock(Item item, InventoryLocation inventoryLocation, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalQuantity, BigDecimal totalItemCost, Date createdAt, Date updatedAt) {
        this.item = item;
        this.inventoryLocation = inventoryLocation;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalQuantity = totalQuantity;
        this.totalItemCost = totalItemCost;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
