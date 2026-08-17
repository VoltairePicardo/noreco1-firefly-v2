package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class AllocationFactor implements Serializable {
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_accountId")
    private Account account;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_businessSegmentId")
    private BusinessSegment businessSegment;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_effectivityDateId")
    private DateRange effectivityDate;

    @Column
    private BigDecimal percentage = BigDecimal.ZERO;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column
    private Date createdAt;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column
    private Date updatedAt;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_factorId")
    private Factor factor;

}
