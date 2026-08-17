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
@Entity(name = "LIHeader")
public class IomasLIHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "h_Id")
    private Integer id;

    @Column(name = "h_Number")
    private Integer number;

    @Column(name = "efd_Id")
    private Integer efdId;

    @Column(name = "h_Date")
    private Date date;

    @Column(name = "h_Remarks")
    private String remarks;

    @Column(name = "h_Total")
    private BigDecimal total;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasLIHeader(Integer number, Date date, String remarks, String loginName) {
        this.number = number;
        this.date = date;
        this.remarks = remarks;
        this.loginName = loginName;
    }

}
