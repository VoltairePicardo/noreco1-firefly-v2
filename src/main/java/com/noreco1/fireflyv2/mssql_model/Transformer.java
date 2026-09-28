package com.noreco1.fireflyv2.mssql_model;

import com.noreco1.fireflyv2.model.enums.Bushing;
import com.noreco1.fireflyv2.model.enums.CoreType;
import com.noreco1.fireflyv2.model.enums.Polarity;
import com.noreco1.fireflyv2.model.enums.TransformerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class Transformer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String serialNo;

    @Column
    private String owner;

    @Column
    private String ownerAddress;

    @Column
    private Integer brand;

    @Column
    private BigDecimal kva;

    @Column
    private Integer primaryVoltage;

    @Column
    private Integer secondaryVoltage;

    @Column
    private BigDecimal impedance;

    @Enumerated(EnumType.STRING)
    @Column
    private Polarity polarity;

    @Enumerated(EnumType.STRING)
    @Column
    private CoreType coreType;

    @Enumerated(EnumType.STRING)
    @Column
    private Bushing bushing;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TransformerType type;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_createdByUserId")
    private User createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;
}
