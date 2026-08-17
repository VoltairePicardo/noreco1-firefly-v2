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
public class MaterialCreditTicketDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Integer matId;

    @Column
    private Integer quantity;

    @Column
    private BigDecimal averageCost;

    @Column
    private BigDecimal total;

    @Column
    private BigDecimal unitCost;

    @Column
    private BigDecimal totalCost;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_materialCreditTicketId", nullable = true, columnDefinition = "0")
    private MaterialCreditTicket materialCreditTicket;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
    private Item item;

    public MaterialCreditTicketDetail(Integer matId, Integer quantity, BigDecimal averageCost, BigDecimal total,
                                      BigDecimal unitCost, BigDecimal totalCost, MaterialCreditTicket materialCreditTicket, Item item) {
        this.matId = matId;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.total = total;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.materialCreditTicket = materialCreditTicket;
        this.item = item;
    }

}
