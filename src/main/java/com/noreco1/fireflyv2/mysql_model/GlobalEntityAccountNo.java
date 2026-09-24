package com.noreco1.fireflyv2.mysql_model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class GlobalEntityAccountNo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer accountNo;

    @Column(nullable = false, length = 50)
    private String entityType;

    @Column(nullable = false)
    private Integer entityId = 0;

    @Column(nullable = false, length = 50)
    private String entitySystem;

    @Column(nullable = false, length = 255)
    private String displayName;

    @Column(nullable = false)
    private Boolean isActive = false;

    @Column(name = "FK_createdByUserId", nullable = false)
    private Integer createdByUserId = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;
}
