package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "ADHeaders")
public class IomasADHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ADh_Id")
    private Integer id;

    @Column(name = "ADh_Number")
    private Integer number;

    @Column(name = "ADh_Date")
    private Date date;

    @Column(name = "ADh_Total")
    private BigDecimal total;

    @Column(name = "ADh_Remarks")
    private String remarks;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasADHeader(Integer number, Date date, BigDecimal total, String remarks, String loginName) {
        this.number = number;
        this.date = date;
        this.total = total;
        this.remarks = remarks;
        this.loginName = loginName;
    }

}
