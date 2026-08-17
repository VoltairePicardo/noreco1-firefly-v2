package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ModeOfProcurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    String name;

    @Column
    Boolean isForPO;

    public ModeOfProcurement(String name, Boolean isForPO) {
        this.name = name;
        this.isForPO = isForPO;
    }

}
