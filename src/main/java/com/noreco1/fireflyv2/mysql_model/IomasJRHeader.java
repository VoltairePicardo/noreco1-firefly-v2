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
@Entity(name = "JRHeaders")
public class IomasJRHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "h_Id")
    private Integer id;

    @Column(name = "h_Number")
    private Integer number;

    @Column(name = "h_Date")
    private Date date;

    @Column(name = "h_Total")
    private BigDecimal total;

    @Column(name = "h_Remarks")
    private String remarks;

    @Column(name = "h_ReleaseTo")
    private String releaseTo;

    @Column(name = "h_Purpose")
    private String purpose;

    @Column(name = "h_RequestedBy")
    private String requestedBy;

    @Column(name = "h_Position1")
    private String position1;

    @Column(name = "h_ReceivedBy")
    private String receivedBy;

    @Column(name = "h_Position2")
    private String position2;

    @Column(name = "h_Approval")
    private String approval;

    @Column(name = "h_Designation")
    private String designation;

    @Column(name = "h_Sales")
    private Boolean sales;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasJRHeader(Integer number, Date date, BigDecimal total, String remarks, String releaseTo, String purpose,
                         String requestedBy, String position1, String receivedBy, String position2, String approval,
                         String designation, Boolean sales, String loginName) {
        this.number = number;
        this.date = date;
        this.total = total;
        this.remarks = remarks;
        this.releaseTo = releaseTo;
        this.purpose = purpose;
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
