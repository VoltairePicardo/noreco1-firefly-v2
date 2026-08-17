package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
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
public class SalvageMaterialRequisition {

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
    private String remarks;

    @Column
    private BigDecimal total;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Column
    private Integer docId;

    public SalvageMaterialRequisition(String number, Date date, String remarks, BigDecimal total, Date createdAt, Integer docId) {
        this.number = number;
        this.date = date;
        this.remarks = remarks;
        this.total = total;
        this.createdAt = createdAt;
        this.docId = docId;
    }

}
