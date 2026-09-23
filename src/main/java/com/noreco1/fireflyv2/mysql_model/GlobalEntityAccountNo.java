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

    /** The actual account number shown to users. Mirrors id for auto-generated entries. */
    @Column(nullable = false, unique = true)
    private Integer accountNo;

    /** CONSUMER | APPLICANT | EMPLOYEE | USER | MEMBER | SUPPLIER | OTHER */
    @Column(nullable = false, length = 50)
    private String entityType;

    /** PK of the entity in its own system. 0 = not yet linked (pre-generated). */
    @Column(nullable = false)
    private Integer entityId = 0;

    /** NORECO1_IBCMS_MSSQL | NORECO1_MYSQL_FIREFLY | NORECO1_FIREFLY_V2 */
    @Column(nullable = false, length = 50)
    private String entitySystem;

    /** Snapshot of entity name at registration time. */
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
