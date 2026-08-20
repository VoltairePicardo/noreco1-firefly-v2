package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.controller.response.StockWithdrawalDetailDto;
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
public class StockWithdrawalDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_stockWithdrawalId", nullable = true, columnDefinition = "0")
    private StockWithdrawal stockWithdrawal;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
    private Item item;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_unitId", nullable = true, columnDefinition = "0")
    private UnitMeasure unit;

    @Column
    private BigDecimal quantity;

    @Column
    private BigDecimal quantityReleased;

    @Column
    private Boolean isSpecialEquipment;

    public StockWithdrawalDetail(StockWithdrawal stockWithdrawal, Item item, UnitMeasure unit, BigDecimal quantity, BigDecimal quantityReleased, boolean isSpecialEquipment) {
        this.stockWithdrawal = stockWithdrawal;
        this.item = item;
        this.unit = unit;
        this.quantity = quantity;
        this.quantityReleased = quantityReleased;
        this.isSpecialEquipment = isSpecialEquipment;
    }

    public StockWithdrawalDetailDto toDto(){
        Item item = getItem();
        UnitMeasure unit = getUnit();
        return new StockWithdrawalDetailDto(
                item != null ? item.getId() : null,
                item != null ? item.getCode() : null,
                unit != null ? unit.getId() : null,
                unit != null ? unit.getCode() : null,
                item != null ? item.getDescription() : null,
                getQuantity(),
                getQuantityReleased(),
                getQuantity(),
                Boolean.TRUE.equals(getIsSpecialEquipment()));
    }

}
