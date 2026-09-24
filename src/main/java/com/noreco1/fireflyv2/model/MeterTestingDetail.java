package com.noreco1.fireflyv2.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeterTestingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_meterTestingId")
    @JsonIgnore
    private MeterTesting meterTesting;

    @Column
    private String meterSerialNo;

    @Column
    private BigDecimal error;

    @Column
    private boolean sta;

    @Column
    private boolean crp;

    @Column
    private boolean voltageTest;

    @Column
    private boolean result;
}
