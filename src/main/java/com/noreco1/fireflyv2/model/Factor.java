package com.noreco1.fireflyv2.model;

import jakarta.persistence.*;

import lombok.*;

import java.util.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Factor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String code;

    @Column
    private String description;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column
    private Date createdAt = new Date();

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column
    private Date updatedAt = new Date();

    @Transient
    private Set<FactorPercentageDistro> factorPercentageDistroSet = new HashSet<>();

    @Transient
    private Map factorPercentageDistroSetByValidity = new HashMap();

    @Transient
    private boolean removable = true;

    @Transient
    private DateRange validityDate;

}
