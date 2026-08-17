package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.sql.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "JAHeaders")
public class IomasJAHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "h_Id")
    private Integer id;

    @Column(name = "h_Number")
    private Integer number;

    @Column(name = "h_Date")
    private Date date;

    @Column(name = "h_Remarks")
    private String remarks;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasJAHeader(Integer number, Date date, String remarks, String loginName) {
        this.number = number;
        this.date = date;
        this.remarks = remarks;
        this.loginName = loginName;
    }

}
