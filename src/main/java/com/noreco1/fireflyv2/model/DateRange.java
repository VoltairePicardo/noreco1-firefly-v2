package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DateRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Temporal(TemporalType.DATE)
    @Column
    private Date start;

    @Temporal(TemporalType.DATE)
    @Column
    private Date end;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable=false, updatable=false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable=false, updatable=false)
    private Date updatedAt;

    @Column
    private String description;

}
