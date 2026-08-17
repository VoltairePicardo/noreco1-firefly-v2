package com.noreco1.fireflyv2.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrder implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Length(min = 3, max = 45)
    @Column(unique = true)
    private String code;

    @NotEmpty(message = "Please enter description")
    @Column
    private String description;

    @Column(name="FK_accountNo")
    private Integer accountNumber;

    @Column
    private Integer year;

    @Column
    private Integer month;

    @NotNull(message = "Please select date")
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date date;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @Column(columnDefinition = "0")
    private Boolean isClosed = false;

    @Column
    private Date closedDatetime;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_closedByUserId")
    private User closedBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_createdByUserId", nullable = true, columnDefinition = "0")
    private User createdBy;

    @Column(nullable = false)
    private Date targetDate;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_SLEntityClassificationId", nullable = false)
    private SLEntityClassification slEntityClassification;

    @Column
    private Integer type;

    @Column
    private String location;

    @Column
    private Integer classification;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_projectId")
    private Project project;

    @Transient
    private Town town;

    @Column
    private Date periodCoveredFrom;

    @Column
    private Date periodCoveredTo;

    public WorkOrder(Integer id) {
        this.id = id;
    }

    public WorkOrder(String code, String description, Integer accountNumber, Integer year, Integer month, Date date,
                     Date createdAt, Date updatedAt, Boolean isClosed, Date closedDatetime, User closedBy,
                     User createdBy, Date targetDate, SLEntityClassification slEntityClassification, Integer type,
                     String location, Integer classification, Project project, Town town, Date periodCoveredFrom, Date periodCoveredTo) {
        this.code = code;
        this.description = description;
        this.accountNumber = accountNumber;
        this.year = year;
        this.month = month;
        this.date = date;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isClosed = isClosed;
        this.closedDatetime = closedDatetime;
        this.closedBy = closedBy;
        this.createdBy = createdBy;
        this.targetDate = targetDate;
        this.slEntityClassification = slEntityClassification;
        this.type = type;
        this.location = location;
        this.classification = classification;
        this.project = project;
        this.town = town;
        this.periodCoveredFrom = periodCoveredFrom;
        this.periodCoveredTo = periodCoveredTo;
    }

}
