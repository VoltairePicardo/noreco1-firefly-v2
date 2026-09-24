package com.noreco1.fireflyv2.mssql_model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Integer accountNo;

    @Column
    private String accountName;

    @Column(name = "oldAcctNo")
    private String oldAccountNo;

    @Column(name = "street")
    private String address;

    public String getOldAccountNo() {
        return oldAccountNo.trim();
    }
}
