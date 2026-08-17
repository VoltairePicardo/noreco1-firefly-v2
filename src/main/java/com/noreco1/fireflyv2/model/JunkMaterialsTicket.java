package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class JunkMaterialsTicket {

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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Column
    private Integer docId;

    public JunkMaterialsTicket(String number, Date date, String remarks, Date createdAt, Integer docId) {
        this.number = number;
        this.date = date;
        this.remarks = remarks;
        this.createdAt = createdAt;
        this.docId = docId;
    }

}
