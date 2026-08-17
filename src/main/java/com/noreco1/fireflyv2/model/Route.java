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
public class Route implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String url;

    @Column
    private String type;

    @Column
    private Boolean restricted = false;

    @Column
    private String description;

    public Route(String url, String type, Boolean restricted, String description) {
        this.url = url;
        this.type = type;
        this.restricted = restricted;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.getDescription();
    }
}
