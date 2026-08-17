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
public class JunkMaterialsReleasingReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Integer matId;

    @Column
    private Integer quantity;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_junkMaterialsReleasingReceiptId", nullable = true, columnDefinition = "0")
    private JunkMaterialsReleasingReceipt junkMaterialsReleasingReceipt;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
    private Item item;

    @Column
    private BigDecimal total;

    @Column
    private BigDecimal averageCost;

    public JunkMaterialsReleasingReceiptDetail(Integer matId, Integer quantity, JunkMaterialsReleasingReceipt junkMaterialsReleasingReceipt,
                                               Item item, BigDecimal total, BigDecimal averageCost) {
        this.matId = matId;
        this.quantity = quantity;
        this.junkMaterialsReleasingReceipt = junkMaterialsReleasingReceipt;
        this.item = item;
        this.total = total;
        this.averageCost = averageCost;
    }

}
