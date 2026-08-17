package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.model.enums.MonthlyCycleStatus;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.validator.constraints.Range;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyCycleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Range(min = 1, max = 12)
    private Integer month;

    @Min(value = 1970)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column
    private MonthlyCycleStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_createdByUserId")
    private User createdBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_monthlyCycleId")
    private MonthlyCycle monthlyCycle;

    public MonthlyCycleLog(Integer month, Integer year, MonthlyCycleStatus status, Date createdAt, User createdBy, MonthlyCycle monthlyCycle) {
        this.month = month;
        this.year = year;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.monthlyCycle = monthlyCycle;
    }

}
