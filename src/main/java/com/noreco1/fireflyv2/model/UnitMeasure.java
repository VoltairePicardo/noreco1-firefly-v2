package com.noreco1.fireflyv2.model;

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
public class UnitMeasure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column(unique = true)
    @NotBlank(message = "Unit code is empty")
    private String code;

    @Column(unique = true)
    @NotBlank(message = "Unit description is empty")
    private String description;

    @Column
    private String plural;

    public UnitMeasure(String code, String description, String plural) {
        this.code = code;
        this.description = description;
        this.plural = plural;
    }

}
