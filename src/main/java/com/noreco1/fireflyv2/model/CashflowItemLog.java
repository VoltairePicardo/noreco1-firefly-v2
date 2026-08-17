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
public class CashflowItemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_cashflowItemId")
    private CashflowItem cashflowItem;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_loggedByUserId")
    private User loggedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false, insertable = false)
    private Date createdAt;

}
