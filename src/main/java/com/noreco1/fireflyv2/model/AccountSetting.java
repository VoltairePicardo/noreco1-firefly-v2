package com.noreco1.fireflyv2.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import lombok.*;

import com.noreco1.fireflyv2.controller.response.AccountSettingDetailDto;

import java.io.Serializable;
import java.util.*;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSetting implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Date date;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_receivingReportId")
    private ReceivingReport receivingReport;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_stockReceiveId")
    private StockReceive stockReceive;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_materialCreditTicketId")
    private MaterialCreditTicket materialCreditTicket;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_stockReleaseId")
    private StockRelease stockRelease;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_materialSalvageTicketId")
    private MaterialSalvageTicket materialSalvageTicket;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_stockAdjustmentId")
    private StockAdjustment stockAdjustment;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_createdByUserId")
    private User createdBy;

    @Column
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Transient
    private Map documentDetail = new HashMap();

    @Transient
    private ArrayList<AccountSettingDetailDto> accountSettingDetails = new ArrayList<>();

    @Transient
    private String code;

    @Column
    private String remarks;

    @Transient
    private Boolean rrConfirmedForJv = false;
}
