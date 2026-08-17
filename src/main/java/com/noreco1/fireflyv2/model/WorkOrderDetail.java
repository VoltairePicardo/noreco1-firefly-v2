package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_workOrderId")
    private WorkOrder workOrder;

    @OneToOne
    @JoinColumn(name = "FK_transactionId")
    private Transaction transaction;

    @NotNull
    @Column
    private BigDecimal labor = BigDecimal.ZERO;

    @NotNull
    @Column
    private BigDecimal overhead = BigDecimal.ZERO;

    @NotNull
    @Column
    private BigDecimal tax = BigDecimal.ZERO;

    @NotNull
    @Column
    private BigDecimal houseConnection = BigDecimal.ZERO;

    @NotNull
    @Column
    private BigDecimal materials = BigDecimal.ZERO;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_createdByUserId")
    private User createdBy;

}
