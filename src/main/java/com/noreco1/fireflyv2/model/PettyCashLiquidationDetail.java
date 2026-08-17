package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class PettyCashLiquidationDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_pettyCashLiquidationId", nullable = true, columnDefinition = "0")
    private PettyCashLiquidation pettyCashLiquidation;

    @Column
    private BigDecimal amount;

    @Column
    private String orNumber;

    @Column
    private String remarks;

    public PettyCashLiquidationDetail(PettyCashLiquidation pettyCashLiquidation, BigDecimal amount, String orNumber, String remarks) {
        this.pettyCashLiquidation = pettyCashLiquidation;
        this.amount = amount;
        this.orNumber = orNumber;
        this.remarks = remarks;
    }

}
