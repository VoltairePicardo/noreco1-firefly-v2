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
public class TransformerVoltageRatioTest {

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
    private BigDecimal primaryVoltageInduce;

    @Column
    private BigDecimal tap1;

    @Column
    private BigDecimal tap2;

    @Column
    private BigDecimal tap3;

    @Column
    private BigDecimal tap4;

    @Column
    private BigDecimal tap5;
}
