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
@Entity(name = "HWIHeaders")
public class IomasHWIHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MRh_Id")
    private Integer id;

    @Column(name = "Sec_Id")
    private Integer secId;

    @Column(name = "MRh_Number")
    private Integer number;

    @Column(name = "MRh_Date")
    private Date date;

    @Column(name = "MRh_ReleaseTo")
    private String releaseTo;

    @Column(name = "MRh_Purpose")
    private String purpose;

    @Column(name = "MRh_Total")
    private BigDecimal total;

    @Column(name = "MRh_RequestedBy")
    private String requestedBy;

    @Column(name = "MRh_Position1")
    private String position1;

    @Column(name = "MRh_ReceivedBy")
    private String receivedBy;

    @Column(name = "MRh_Position2")
    private String position2;

    @Column(name = "MRh_WONumber")
    private String WONumber;

    @Column(name = "MRh_AcctNumber")
    private String acctNumber;

    @Column(name = "MRh_Approval")
    private String approval;

    @Column(name = "MRh_Designation")
    private String designation;

    @Column(name = "MRh_JobOrderNo")
    private String jobOrderNo;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasHWIHeader(Integer secId, Integer number, Date date, String releaseTo, String purpose, BigDecimal total,
                          String requestedBy, String position1, String receivedBy, String position2, String WONumber,
                          String acctNumber, String approval, String designation, String jobOrderNo, String loginName) {
        this.secId = secId;
        this.number = number;
        this.date = date;
        this.releaseTo = releaseTo;
        this.purpose = purpose;
        this.total = total;
        this.requestedBy = requestedBy;
        this.position1 = position1;
        this.receivedBy = receivedBy;
        this.position2 = position2;
        this.WONumber = WONumber;
        this.acctNumber = acctNumber;
        this.approval = approval;
        this.designation = designation;
        this.jobOrderNo = jobOrderNo;
        this.loginName = loginName;
    }

}
