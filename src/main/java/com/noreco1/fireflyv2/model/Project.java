package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Project extends DocumentNoApproval implements Serializable {

    @Column
    private Integer year;

    @Column
    private String name;

    @Column
    private String location;

    @Column
    private String purpose;

    @Column
    private String projectManager;

    @Column
    private String paymentDetails;

    @Column
    private Date date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_projectFundingId")
    private ProjectFunding projectFunding;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_townId")
    private Town town;

    @Transient
    private String costEstimateCode;

    @Transient
    private String billOfMaterialCode;

    @Transient
    private String workOrderCode;

    @Transient
    private String siteInspectionReportCode;

    @Transient
    private String projectAcceptanceReportCode;

    @Transient
    private String projectAcceptanceCertificationCode;

    @Transient
    private String assetCode;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_departmentId")
    private Department department;

    @Column
    private String consumerAccountNumber;

    @Column
    private String consumerName;

    @Transient
    private String contractorsStr;

    @Transient
    private List<ProjectContractor> contractors = new ArrayList<>();

    @Transient
    private int type;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_projectTypeId")
    private ProjectType projectType;

    @Column
    private Date periodCoveredFrom;

    @Column
    private Date periodCoveredTo;

    @Column
    private String governmentOfficeName;

    public Project(Integer year, String name, String location, String purpose, String projectManager, String paymentDetails, Date date, ProjectFunding projectFunding, Town town, String costEstimateCode, String workOrderCode, String siteInspectionReportCode, String projectAcceptanceReportCode, String projectAcceptanceCertificationCode, String assetCode, String consumerAccountNumber, String consumerName, Department department, ProjectType projectType, Date periodCoveredFrom, Date periodCoveredTo, String governmentOfficeName) {
        this.year = year;
        this.name = name;
        this.location = location;
        this.purpose = purpose;
        this.projectManager = projectManager;
        this.paymentDetails = paymentDetails;
        this.date = date;
        this.projectFunding = projectFunding;
        this.town = town;
        this.department = department;
        this.consumerAccountNumber = consumerAccountNumber;
        this.consumerName = consumerName;
        this.projectType = projectType;
        this.periodCoveredFrom = periodCoveredFrom;
        this.periodCoveredTo = periodCoveredTo;
        this.governmentOfficeName = governmentOfficeName;
    }

}
