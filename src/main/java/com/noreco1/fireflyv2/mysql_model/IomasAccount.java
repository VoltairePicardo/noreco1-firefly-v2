package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "AccountCode")
public class IomasAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "a_Id")
    private Integer id;

    @Column(name = "a_Code")
    private String code;

    @Column(name = "a_Desc")
    private String description;

    @Column(name = "a_Type")
    private String type;

    public IomasAccount(String code, String description, String type) {
        this.code = code;
        this.description = description;
        this.type = type;
    }

}
