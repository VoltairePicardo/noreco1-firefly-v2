package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSheetSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String description;

    @Column
    private Integer level;

    @Column
    private BigDecimal sequence;

    @Column
    private Integer parentId;

    @Column
    private Boolean hasChild;

    @Column
    private Character column = 'L';

    @Column
    private String sumOfRows;

    @Column
    private boolean show;

}
