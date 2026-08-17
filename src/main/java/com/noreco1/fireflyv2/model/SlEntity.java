package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.stereotype.Component;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "slentity")
@NoArgsConstructor
@AllArgsConstructor
public class SlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    Integer accountNo;

    @Column
    String name;

    @Column
    String address;

    @Column
    String slEntityClassification;

    @Column
    int marker;

    @Column
    Boolean vatable;

    public SlEntity(String name, String address, String slEntityClassification, int marker, Boolean vatable) {
        this.name = name;
        this.address = address;
        this.slEntityClassification = slEntityClassification;
        this.marker = marker;
        this.vatable = vatable;
    }

}
