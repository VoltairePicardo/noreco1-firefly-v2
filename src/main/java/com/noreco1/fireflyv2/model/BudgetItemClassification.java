package com.noreco1.fireflyv2.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class BudgetItemClassification implements Serializable{

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    @NotEmpty
    private String description;

}
