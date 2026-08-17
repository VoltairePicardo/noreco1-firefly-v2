package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column(name = "FK_accountNo")
    private Integer accountNo;

    @Column(unique = true)
    private String refNo;

    @Column
    private String description;

    @Column
    private Date acquisitionDate;

    @Column
    private BigDecimal totalValue;

    @Column
    private Integer depreciationMonths = 0;

    @Column
    private BigDecimal depreciationYears = BigDecimal.ZERO;

    @Column
    private Integer startYear;

    @Column
    private Integer startMonth;

    @Column
    private Integer endYear;

    @Column
    private Integer endMonth;

    @Column
    private BigDecimal totalMonthlyDepreciation;

    @Column
    private BigDecimal totalDepreciatedValue;

    @Column
    private BigDecimal annualDepreciationRate = BigDecimal.ZERO;

    @Column
    private BigDecimal monthlyDepreciationRate = BigDecimal.ZERO;

    @Column
    private BigDecimal totalRemainingValue;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_createdByUserId", nullable = true, columnDefinition = "0")
    private User createdBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_workOrderId", nullable = true, columnDefinition = "0")
    private WorkOrder workOrder;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Transient
    List<Map> assetDetails = new ArrayList<>();

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_SLEntityClassificationId", nullable = false)
    private SLEntityClassification slEntityClassification;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_assetTypeId")
    private AssetType assetType;

    @Column
    private String location;

    @Column
    private Integer year;

    @Column
    private String code;

    @Column
    private String retirementRemarks;

    @Column
    private String status;

    public BigDecimal setDefaultAnnualDepreciationRate() {
        try {
            BigDecimal divisor = this.getDepreciationYears();
            BigDecimal dividend = new BigDecimal(100);
            this.annualDepreciationRate = dividend.divide(divisor, 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return this.annualDepreciationRate;
        }
    }

    public BigDecimal setDefaultMonthlyDepreciationRate() {
        try {
            Integer divisor = this.getDepreciationMonths();
            BigDecimal dividend = new BigDecimal(100);
            this.monthlyDepreciationRate = dividend.divide(new BigDecimal(divisor), 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return this.monthlyDepreciationRate;
        }
    }

}
