package com.noreco1.fireflyv2.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class TransformerLossTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_transformerTestingId")
    @JsonIgnore
    private TransformerTesting transformerTesting;

    @Column
    private BigDecimal shortCircuitPrimaryCurrent;

    @Column
    private BigDecimal shortCircuitResult;

    @Column
    private BigDecimal openCircuitSecondaryVoltage;

    @Column
    private BigDecimal openCircuitResult;

    @Column
    private BigDecimal totalLoss;

    @Column
    private BigDecimal iex;

    @Column
    private BigDecimal iz;

    @Column
    private BigDecimal ir;

    @Column
    private BigDecimal ix;

    @Column
    private BigDecimal eff;
}
