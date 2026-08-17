package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import com.noreco1.fireflyv2.common.helpers.Checker;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
public class Consumer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="consumerid")
    private Integer id;

    @Column
    private Integer acctNo;

    @Column
    private String acctnum;

    @Column
    private String towncode;

    @Column(name="name")
    private String acctName;

    @Column(name="routecode")
    private String routeCode;

    @Column(name="addr")
    private String address;

    public Consumer(){}

    public Consumer(Integer acctNo, String acctName, String address, String routeCode, String acctnum, String towncode) {
        this.acctNo = acctNo;
        this.acctName = acctName;
        this.address = address;
        this.routeCode = routeCode;
        this.acctnum = acctnum;
        this.towncode = towncode;
    }

    public String getOldAccountNo() {
        StringBuilder builder = new StringBuilder();

        if(!Checker.isStringNullAndEmpty(this.getTowncode())) {
            builder.append(this.getTowncode());
            builder.append("-");
        }

        if(!Checker.isStringNullAndEmpty(this.getRouteCode())) {
            builder.append(this.getRouteCode());
            builder.append("-");
        }
        if(!Checker.isStringNullAndEmpty(this.getAcctnum())) {
            builder.append(this.getAcctnum());
        }

        return builder.toString();
    }
}

