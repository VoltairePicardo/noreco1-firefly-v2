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
@NoArgsConstructor
@Entity
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class SalvageMaterialRequisitionDetail {

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

        @NotFound(action = NotFoundAction.IGNORE)
        @ManyToOne
        @JoinColumn(name="FK_salvageMaterialRequisitionId", nullable = true, columnDefinition = "0")
        private SalvageMaterialRequisition salvageMaterialRequisition;

        @NotFound(action = NotFoundAction.IGNORE)
        @ManyToOne
        @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
        private Item item;

        public SalvageMaterialRequisitionDetail(Integer matId, Integer quantity, BigDecimal averageCost, BigDecimal total,
                                                SalvageMaterialRequisition salvageMaterialRequisition, Item item) {
            this.matId = matId;
            this.quantity = quantity;
            this.averageCost = averageCost;
            this.total = total;
            this.salvageMaterialRequisition = salvageMaterialRequisition;
            this.item = item;
        }

    }
