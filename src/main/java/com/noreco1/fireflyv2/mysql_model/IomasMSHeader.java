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
@Entity(name = "MSHeaders")
public class IomasMSHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MSh_Id")
    private Integer id;

    @Column(name = "MSh_Number")
    private Integer number;

    @Column(name = "MSh_Date")
    private Date date;

    @Column(name = "MSh_Remarks")
    private String remarks;

    @Column(name = "MSh_ReturnedBy")
    private String returnedBy;

    @Column(name = "MSh_Position")
    private String position;

    @Column(name = "EFD_Id")
    private Integer EFDId;

    @Column(name = "MSh_FromMR")
    private Integer fromMR;

    @Column(name = "h_WONo")
    private String WONo;

    @Column(name = "h_AcctNo")
    private Boolean acctNo;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasMSHeader(Integer number, Date date, String remarks, String returnedBy, String position, Integer EFDId,
                         Integer fromMR, String WONo, Boolean acctNo, String loginName) {
        this.number = number;
        this.date = date;
        this.remarks = remarks;
        this.returnedBy = returnedBy;
        this.position = position;
        this.EFDId = EFDId;
        this.fromMR = fromMR;
        this.WONo = WONo;
        this.acctNo = acctNo;
        this.loginName = loginName;
    }

}
