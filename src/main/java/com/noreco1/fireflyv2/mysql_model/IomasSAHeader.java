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
@Entity(name = "SAHeaders")
public class IomasSAHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SAh_Id")
    private Integer id;

    @Column(name = "SAh_Number")
    private Integer number;

    @Column(name = "SAh_Date")
    private Date date;

    @Column(name = "SAh_Remarks")
    private String remarks;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasSAHeader(Integer number, Date date, String remarks, String loginName) {
        this.number = number;
        this.date = date;
        this.remarks = remarks;
        this.loginName = loginName;
    }

}
