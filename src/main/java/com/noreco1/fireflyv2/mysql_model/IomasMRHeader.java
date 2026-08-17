package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "MRHeaders")
public class IomasMRHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MRh_Id")
    private Integer id;

    @Column(name = "MRh_Number")
    private String number;

    @Temporal(TemporalType.DATE)
    @Column(name = "Mrh_Date")
    private Date date;

    @Column(name = "Mrh_Purpose")
    private String purpose;

    @Column(name = "Mrh_Total")
    private BigDecimal total;

    @Column(name = "MRh_ReleaseTo")
    private String releaseTo;

    public IomasMRHeader(String number, Date date, String purpose, BigDecimal total, String releaseTo) {
        this.number = number;
        this.date = date;
        this.purpose = purpose;
        this.total = total;
        this.releaseTo = releaseTo;
    }

}
