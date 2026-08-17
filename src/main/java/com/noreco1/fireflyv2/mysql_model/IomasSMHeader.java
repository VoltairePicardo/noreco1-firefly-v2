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
@Entity(name = "SMHeaders")
public class IomasSMHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SMh_Id")
    private Integer id;

    @Column(name = "SMh_Number")
    private Integer number;

    @Column(name = "SMh_Date")
    private Date date;

    @Column(name = "h_Total")
    private BigDecimal total;

    @Column(name = "SMh_ReleaseTo")
    private String releaseTo;

    @Column(name = "SMh_Purpose")
    private String purpose;

    @Column(name = "SMh_Remarks")
    private String remarks;

    @Column(name = "SMh_RequestedBy")
    private String requestedBy;

    @Column(name = "SMh_Position1")
    private String position1;

    @Column(name = "SMh_ReceivedBy")
    private String receivedBy;

    @Column(name = "SMh_Position2")
    private String position2;

    @Column(name = "SMh_Approval")
    private String approval;

    @Column(name = "SMh_Designation")
    private String designation;

    @Column(name = "h_Sales")
    private Boolean sales;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasSMHeader(Integer number, Date date, BigDecimal total, String releaseTo, String purpose, String remarks,
                         String requestedBy, String position1, String receivedBy, String position2, String approval,
                         String designation, Boolean sales, String loginName) {
        this.number = number;
        this.date = date;
        this.total = total;
        this.releaseTo = releaseTo;
        this.purpose = purpose;
        this.remarks = remarks;
        this.requestedBy = requestedBy;
        this.position1 = position1;
        this.receivedBy = receivedBy;
        this.position2 = position2;
        this.approval = approval;
        this.designation = designation;
        this.sales = sales;
        this.loginName = loginName;
    }

}