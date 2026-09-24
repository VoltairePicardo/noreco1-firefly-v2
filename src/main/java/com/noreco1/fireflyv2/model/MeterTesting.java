package com.noreco1.fireflyv2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.noreco1.fireflyv2.controller.response.AssemblyUnitDetailDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeterTesting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Temporal(TemporalType.DATE)
    @Column
    private Date date;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_meterModelId")
    private MeterModel meterModel;

    @Column
    private String temperature;

    @Column
    private String relativeHumidity;

    @Column
    private Integer multiplier;

    @Column
    private String testedBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_createdByUserId")
    private User createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @Column(name = "FK_consumerAccountNo")
    private Integer accountNo;

    @Column
    private BigDecimal presentReading;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_meterCalibratorId")
    private User meterCalibrator;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<MeterTestingDetail> details = new LinkedList<>();

}
