package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MaterialIssueRegisterDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Column
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_materialIssueRegisterId", referencedColumnName = "id")
    private MaterialIssueRegister materialIssueRegister;

    @Column
    private Integer quantity;

    @Column
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column
    private BigDecimal amount = BigDecimal.ZERO;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
    private Item item;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_unitMeasureId", nullable = true, columnDefinition = "0")
    private UnitMeasure unitMeasure;

    public MaterialIssueRegisterDetail(String description, MaterialIssueRegister materialIssueRegister, Integer quantity,
                                       BigDecimal unitPrice, BigDecimal amount, Item item, UnitMeasure unitMeasure) {
        this.description = description;
        this.materialIssueRegister = materialIssueRegister;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.item = item;
        this.unitMeasure = unitMeasure;
    }

}
