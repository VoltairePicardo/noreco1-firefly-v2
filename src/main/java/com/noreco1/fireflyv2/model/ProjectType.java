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
public class ProjectType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String description;

    public ProjectType(String description) {
        this.description = description;
    }

}
