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
@Entity(name = "JMHeaders")
public class IomasJMHeader {

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

    @Column(name = "h_ReturnedBy")
    private String returnedBy;

    @Column(name = "h_Position")
    private String position;

    @Column(name = "EFD_Id")
    private Integer EFDId;

    @Column(name = "h_FromMR")
    private Integer fromMR;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasJMHeader(Integer number, Date date, String remarks, String returnedBy, String position, Integer EFDId,
                         Integer fromMR, String loginName) {
        this.number = number;
        this.date = date;
        this.remarks = remarks;
        this.returnedBy = returnedBy;
        this.position = position;
        this.EFDId = EFDId;
        this.fromMR = fromMR;
        this.loginName = loginName;
    }

}