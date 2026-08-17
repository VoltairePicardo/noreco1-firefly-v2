package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Created by yer on 9/19/2016.
 *
 * Statement of Account
 */

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "SAHeader")
public class IomasSOAHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "h_Id")
    private Integer id;

    @Column(name = "h_Number")
    private Integer number;

    @Column(name = "h_Date")
    private Date date;

    @Column(name = "h_To")
    private String to;

    @Column(name = "h_Address")
    private String address;

    @Column(name = "h_Attn")
    private String attn;

    @Column(name = "h_Designation")
    private String designation;

    @Column(name = "h_Purpose")
    private String purpose;

    @Column(name = "h_Remarks")
    private String remarks;

    @Column(name = "h_PreparedBy")
    private String preparedBy;

    @Column(name = "h_PreparedPos")
    private String preparedPos;

    @Column(name = "h_CheckedBy")
    private String checkedBy;

    @Column(name = "h_CheckedPos")
    private String checkedPos;

    @Column(name = "h_PayAmt1")
    private BigDecimal payAmount;

    @Column(name = "h_LaborCost")
    private BigDecimal laborCost;

    @Column(name = "h_VAT")
    private Boolean vat;

    @Column(name = "usr_LoginName")
    private String loginName;

    public IomasSOAHeader(Integer number, Date date, String to, String address, String attn, String designation,
                          String purpose, String remarks, String preparedBy, String preparedPos, String checkedBy,
                          String checkedPos, BigDecimal payAmount, BigDecimal laborCost, Boolean vat, String loginName) {
        this.number = number;
        this.date = date;
        this.to = to;
        this.address = address;
        this.attn = attn;
        this.designation = designation;
        this.purpose = purpose;
        this.remarks = remarks;
        this.preparedBy = preparedBy;
        this.preparedPos = preparedPos;
        this.checkedBy = checkedBy;
        this.checkedPos = checkedPos;
        this.payAmount = payAmount;
        this.laborCost = laborCost;
        this.vat = vat;
        this.loginName = loginName;
    }

}