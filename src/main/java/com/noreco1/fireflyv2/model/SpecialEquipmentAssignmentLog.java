package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class SpecialEquipmentAssignmentLog {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Date date;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_stockReleaseId")
    private StockRelease stockRelease;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_createdByUserId")
    private User createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date updatedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date loggedAt;

    @Column
    private boolean hasConnectOrder;

    @Column
    private String status;

    @Column(name="FK_townId")
    private int townId;

    @Column
    private String town;

    @Column(name="FK_barangayId")
    private int barangayId;

    @Column
    private String barangay;

    @Column(name="FK_sitioId")
    private int sitioId;

    @Column
    private String sitio;

    @Column
    private String street;

    @Column
    private String poleNumber;

    @Column
    private String location;

    @Column
    private boolean soleOwner;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "FK_specialEquipmentAssignmentId")
    private int specialEquipmentAssignmentId;   // to avoid querying of SpecialEquipmentAssignment

    @Column
    private String loggedBy;

    @Transient
    private List<Map> detailsMapList = new ArrayList<>();

}
