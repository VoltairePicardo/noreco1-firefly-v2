package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity(name = "Materials")
public class IomasMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Mat_Id")
    private Integer id;

    @Column(name = "Mat_Code")
    private String code;

    @Column(name = "Mat_Desc")
    private String description;

    @Column(name = "Mat_Units")
    private String unit;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "a_Id")
    private IomasAccount account;

    public IomasMaterial(String code, String description, String unit, IomasAccount account) {
        this.code = code;
        this.description = description;
        this.unit = unit;
        this.account = account;
    }

}
