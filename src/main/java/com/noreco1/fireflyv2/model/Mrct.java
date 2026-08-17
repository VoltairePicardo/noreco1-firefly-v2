package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Mrct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String number;

    @Temporal(TemporalType.DATE)
    @Column
    private Date date;

    @Column
    private String purpose;

    @Column
    private BigDecimal total;

    @Column
    private String releaseTo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Column
    private Integer docId;

    public Mrct(String number, Date date, String purpose, BigDecimal total, String releaseTo, Date createdAt, Integer docId) {
        this.number = number;
        this.date = date;
        this.purpose = purpose;
        this.total = total;
        this.releaseTo = releaseTo;
        this.createdAt = createdAt;
        this.docId = docId;
    }

}
