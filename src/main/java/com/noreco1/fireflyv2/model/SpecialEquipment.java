package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
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
public class SpecialEquipment implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_specialEquipmentTypeId")
    private SpecialEquipmentType specialEquipmentType;

    @Column
    private String serialNo;

    @Column
    private String owner;

    @Column
    private String address;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_brandId")
    private Brand brand;

    @Column
    private Date dateManufactured;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_rrDetailId")
    private ReceivingReportDetail receivingReportDetail;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_kvaRatingId")
    private KvaRating kvaRating;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_primaryVoltageId")
    private Voltage primaryVoltage;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_secondaryVoltageId")
    private Voltage secondaryVoltage;

    @Column
    private BigDecimal impedance;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_frequencyId")
    private Frequency frequency;

    @Column
    private String insulatingOil;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_polarityId")
    private Polarity polarity;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_meterTypeId")
    private MeterType meterType;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_accuracyClassId")
    private AccuracyClass accuracyClass;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_phaseId")
    private Phase phase;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_ratedVoltageId")
    private Voltage ratedVoltage;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_ratedCurrentId")
    private Current ratedCurrent;

    @Column
    private BigDecimal impulse;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_ratedFrequencyId")
    private Frequency ratedFrequency;

    @Column
    private Integer batchNo;

    @Column
    private BigDecimal multiplier;

    @Column
    private BigDecimal initialReading;

    @Column
    private String manufacturer;

    @Column
    private String basicInsulationLevel;

    @Column
    private String ratio;

    @Column
    private String meteringCore;

    @Column
    private String protectionCore;

    @Column
    private String thermal;

    @Column
    private String factor;

    @Column
    private String weight;

    @Column
    private String kvarRating;

    @Column
    private String caseMaterial;

    @Column
    private String ratedPowerFrequency;

    @Column
    private String withstandVoltage;

    @Column
    private String maxInterruptingCurrent;

    @Column
    private String ratedDurationOfShortCircuit;

    @Column
    private String momentaryCurrent;

    @Column
    private String openingTime;

    @Column
    private String totalBreakTime;

    @Column
    private String closingTime;

    @Column
    private String airTempRange;

    @Column
    private String closingVoltage;

    @Column
    private String minOperation;

    @Column
    private String protectionClass;

    @Column
    private String mvaRating;

    @Column
    private String primaryCurrentRating;

    @Column
    private String secondaryCurrentRating;

    @Column
    private String tappingsOnHvVariation;

    @Column
    private String bilHv;

    @Column
    private String bilLv;

    @Column
    private String transformerClass;

    @Column
    private String percentZ;

    @Column
    private String insulationFluid;

    @Column
    private String oilVolume;

    @Column
    private String vectorGroup;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_createdByUserId")
    private User createdBy;

    @Column(updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_specialEquipmentStatusId")
    private SpecialEquipmentStatus status;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_transactionId")
    private Transaction transaction;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_itemTestingDetailId")
    private ItemTestingDetail itemForTesting;

    @Column
    private String deliveryReceiptNumber;

    @Column
    private String invoiceNumber;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_batchId")
    private SpecialEquipmentBatch batch;

    @Column
    private String rating;

    @Column(columnDefinition = "TEXT")
    private String additionalSpecs;

    public SpecialEquipment(SpecialEquipmentType specialEquipmentType, String serialNo, String owner, String address, Brand brand, Date dateManufactured, ReceivingReportDetail receivingReportDetail, KvaRating kvaRating, Voltage primaryVoltage, Voltage secondaryVoltage, BigDecimal impedance, Frequency frequency, String insulatingOil, Polarity polarity, MeterType meterType, AccuracyClass accuracyClass, Phase phase, Voltage ratedVoltage, Current ratedCurrent, BigDecimal impulse, Frequency ratedFrequency, Integer batchNo, BigDecimal multiplier, BigDecimal initialReading, String manufacturer, String basicInsulationLevel, String ratio, String meteringCore, String protectionCore, String thermal, String factor, String weight, String kvarRating, String caseMaterial, String ratedPowerFrequency, String withstandVoltage, String maxInterruptingCurrent, String ratedDurationOfShortCircuit, String momentaryCurrent, String openingTime, String totalBreakTime, String closingTime, String airTempRange, String closingVoltage, String minOperation, String protectionClass, String mvaRating, String primaryCurrentRating, String secondaryCurrentRating, String tappingsOnHvVariation, String bilHv, String bilLv, String transformerClass, String percentZ, String insulationFluid, String oilVolume, String vectorGroup, User createdBy, Date createdAt, Date updatedAt, SpecialEquipmentStatus status, Transaction transaction, ItemTestingDetail itemForTesting, String deliveryReceiptNumber, String invoiceNumber, SpecialEquipmentBatch batch, String rating, String additionalSpecs) {
        this.specialEquipmentType = specialEquipmentType;
        this.serialNo = serialNo;
        this.owner = owner;
        this.address = address;
        this.brand = brand;
        this.dateManufactured = dateManufactured;
        this.receivingReportDetail = receivingReportDetail;
        this.kvaRating = kvaRating;
        this.primaryVoltage = primaryVoltage;
        this.secondaryVoltage = secondaryVoltage;
        this.impedance = impedance;
        this.frequency = frequency;
        this.insulatingOil = insulatingOil;
        this.polarity = polarity;
        this.meterType = meterType;
        this.accuracyClass = accuracyClass;
        this.phase = phase;
        this.ratedVoltage = ratedVoltage;
        this.ratedCurrent = ratedCurrent;
        this.impulse = impulse;
        this.ratedFrequency = ratedFrequency;
        this.batchNo = batchNo;
        this.multiplier = multiplier;
        this.initialReading = initialReading;
        this.manufacturer = manufacturer;
        this.basicInsulationLevel = basicInsulationLevel;
        this.ratio = ratio;
        this.meteringCore = meteringCore;
        this.protectionCore = protectionCore;
        this.thermal = thermal;
        this.factor = factor;
        this.weight = weight;
        this.kvarRating = kvarRating;
        this.caseMaterial = caseMaterial;
        this.ratedPowerFrequency = ratedPowerFrequency;
        this.withstandVoltage = withstandVoltage;
        this.maxInterruptingCurrent = maxInterruptingCurrent;
        this.ratedDurationOfShortCircuit = ratedDurationOfShortCircuit;
        this.momentaryCurrent = momentaryCurrent;
        this.openingTime = openingTime;
        this.totalBreakTime = totalBreakTime;
        this.closingTime = closingTime;
        this.airTempRange = airTempRange;
        this.closingVoltage = closingVoltage;
        this.minOperation = minOperation;
        this.protectionClass = protectionClass;
        this.mvaRating = mvaRating;
        this.primaryCurrentRating = primaryCurrentRating;
        this.secondaryCurrentRating = secondaryCurrentRating;
        this.tappingsOnHvVariation = tappingsOnHvVariation;
        this.bilHv = bilHv;
        this.bilLv = bilLv;
        this.transformerClass = transformerClass;
        this.percentZ = percentZ;
        this.insulationFluid = insulationFluid;
        this.oilVolume = oilVolume;
        this.vectorGroup = vectorGroup;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.transaction = transaction;
        this.itemForTesting = itemForTesting;
        this.deliveryReceiptNumber = deliveryReceiptNumber;
        this.invoiceNumber = invoiceNumber;
        this.batch = batch;
        this.rating = rating;
        this.additionalSpecs = additionalSpecs;
    }

}
