package com.noreco1.fireflyv2.model;

import lombok.*;

import com.noreco1.fireflyv2.mysql_model.*;
import com.noreco1.fireflyv2.mysql_model.Town;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class SpecialEquipmentAssignment implements Serializable {

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

    @Column(updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @Transient
    private List<SpecialEquipmentAssignmentDetail> specialEquipmentAssignmentDetails = new ArrayList<>();

    @Transient
    private boolean hasAccomplishment;

    @Column
    private boolean hasConnectOrder;

    @Transient
    private Consumer consumer;

    @Transient
    private boolean isRevoke = false;

    @Column(name="FK_townId")
    private Integer townId;

    @Column(name="FK_barangayId")
    private Integer barangayId;

    @Column(name="FK_sitioId")
    private Integer sitioId;

    @Column
    private String street;

    @Column
    private String poleNumber;

    @Column
    private String location;

    @Column
    private boolean soleOwner;

    @Transient
    private com.noreco1.fireflyv2.mysql_model.Town town;

    @Transient
    private Barangay barangay;

    @Transient
    private Sitio sitio;

    @Transient
    private List<SpecialEquipmentAssignmentLog> specialEquipmentAssignmentLogs = new ArrayList<>();

}
