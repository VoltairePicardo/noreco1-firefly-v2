package com.noreco1.fireflyv2.mysql_model;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity(name = "MysqlTown")
public class Town implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String townCode;

    @Column
    private String townName;

    public Town(){}

    public Town(String townCode, String townName) {
        this.townCode = townCode;
        this.townName = townName;
    }

}