package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.model.enums.MonthlyCycleStatus;
import org.hibernate.validator.constraints.Range;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(uniqueConstraints = { @UniqueConstraint( columnNames = { "year", "month" } ) } )
@NoArgsConstructor
@AllArgsConstructor
public class  MonthlyCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Range(min = 1, max = 12)
    @Column
    private Integer month;

    @Min(value = 1970)
    @Column
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column
    private MonthlyCycleStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    public MonthlyCycle(Integer month, Integer year, MonthlyCycleStatus status, Date createdAt, Date updatedAt) {
        this.month = month;
        this.year = year;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
