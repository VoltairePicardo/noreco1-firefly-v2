package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity(name = "CheckVoucherReleasedCheque")
@NoArgsConstructor
@AllArgsConstructor
public class ReleasedCheque implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_checkVoucherChequeId")
    private CheckVoucherCheque check;

    @Column
    private String receivedBy;

    @Column
    private Date dateReleased;

    @Column
    private String orNumber;

    @Column
    private String remarks;

    @Column
    private String personImage;

    @Column
    private String signature;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_createdByUserId", nullable = true, columnDefinition = "0")
    private User createdBy;

    @Column
    private String idNumber;

    @Column
    private String depositSlip;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_transactionId")
    private Transaction transaction;

    @Transient
    private String status;

    public ReleasedCheque(CheckVoucherCheque check, String receivedBy, Date dateReleased, String orNumber, String remarks,
                          String personImage, String signature, Date createdAt, Date updatedAt, User createdBy,
                          String idNumber, String depositSlip, Transaction transaction) {
        this.check = check;
        this.receivedBy = receivedBy;
        this.dateReleased = dateReleased;
        this.orNumber = orNumber;
        this.remarks = remarks;
        this.personImage = personImage;
        this.signature = signature;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.idNumber = idNumber;
        this.depositSlip = depositSlip;
    }

}
