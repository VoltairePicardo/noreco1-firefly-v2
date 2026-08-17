package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.controller.response.StockReleaseDetailDto;
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
public class StockReleaseDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_stockReleaseId", nullable = true, columnDefinition = "0")
    private StockRelease stockRelease;

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
    private BigDecimal quantityOrdered;

    @Column
    private BigDecimal quantityReleased;

    public StockReleaseDetail(StockRelease stockRelease, Item item, UnitMeasure unit, BigDecimal quantityOrdered, BigDecimal quantityReleased) {
        this.stockRelease = stockRelease;
        this.item = item;
        this.unit = unit;
        this.quantityOrdered = quantityOrdered;
        this.quantityReleased = quantityReleased;
    }

    public StockReleaseDetailDto toDto(){
        return new StockReleaseDetailDto(getItem().getId(), getItem().getCode(), getUnit().getId(), getUnit().getCode(), getItem().getDescription(), getQuantityOrdered(), getQuantityReleased());
    }
}
