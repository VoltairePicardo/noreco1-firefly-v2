package com.noreco1.fireflyv2.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AssetVoucherLinkType implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    @NotEmpty
    private String description;


}