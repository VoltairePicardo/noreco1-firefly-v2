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
@Entity(name = "MCHeaders")
public class IomasMCHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MCh_Id")
    private Integer id;

    @Column(name = "MCh_Number")
    private Integer number;

    @Column(name = "MCh_Date")
    private Date date;

    @Column(name = "MCh_Remarks")
    private String remarks;

    @Column(name = "MCh_Total")
    private BigDecimal total;

    @Column(name = "MCh_ReturnedBy")
    private String returnedBy;

    @Column(name = "MCh_Position")
    private String position;

    @Column(name = "MCh_WONumber")
    private String woNumber;

    @Column(name = "MCh_MRCTNo")
    private Integer MRCTNo;

    @Column(name = "EFD_Id")
    private Integer EFDId;

    @Column(name = "MCh_FromMR")
    private Integer fromMR;

    @Column(name = "MCh_AcctNumber")
    private String acctNumber;

    @Column(name = "MCh_Sales")
    private Boolean sales;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasMCHeader(Integer number, Date date, String remarks, BigDecimal total, String returnedBy, String position,
                         String woNumber, Integer MRCTNo, Integer EFDId, Integer fromMR, String acctNumber, Boolean sales,
                         String loginName) {
        this.number = number;
        this.date = date;
        this.remarks = remarks;
        this.total = total;
        this.returnedBy = returnedBy;
        this.position = position;
        this.woNumber = woNumber;
        this.MRCTNo = MRCTNo;
        this.EFDId = EFDId;
        this.fromMR = fromMR;
        this.acctNumber = acctNumber;
        this.sales = sales;
        this.loginName = loginName;
    }

}
