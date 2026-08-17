package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Mode implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String description;

    public Mode(String description) {
        this.description = description;
    }

}
