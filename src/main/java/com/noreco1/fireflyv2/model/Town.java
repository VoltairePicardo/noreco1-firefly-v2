package com.noreco1.fireflyv2.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Town implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    @NotEmpty
    private String name;

    @NotEmpty
    @Column
    private String code;

    public Town(String name, String code) {
        this.name = name;
        this.code = code;
    }

}