package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFunding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String description;

    @Column
    private BigDecimal markup = BigDecimal.ZERO;

    public ProjectFunding(String description, BigDecimal markup) {
        this.description = description;
        this.markup = markup;
    }

}
