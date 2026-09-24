package com.noreco1.fireflyv2.mssql_model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeterModel {

    @Id
    @Column
    private Integer id;

    @Column(name = "FK_brandId")
    private Integer brand;

    @Column(name = "FK_meterTypeId")
    private Integer meterType;

    @Column(name = "FK_phaseId")
    private Integer phase;

    @Column(name = "FK_currentRatingId")
    private Integer currentRating;

    @Column(name = "FK_accuracyClassId")
    private Integer accuracyClass;

    @Column(name = "FK_meterFormId")
    private Integer meterForm;

    @Column
    private String modelName;

    @Column
    private BigDecimal constant;

    @Column
    private BigDecimal amperage;

    @Column
    private BigDecimal voltage;

}
