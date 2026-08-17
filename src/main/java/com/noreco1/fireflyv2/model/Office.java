package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotBlank
    @Column(unique = true)
    private String acronym;

    @Column
    private String address;

    @Column
    private String contactNumbers;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_inventoryLocationId")
    private InventoryLocation inventoryLocation;

    public Office(String name, String acronym, String address, String contactNumbers, Date createdAt, Date updatedAt, InventoryLocation inventoryLocation) {
        this.name = name;
        this.acronym = acronym;
        this.address = address;
        this.contactNumbers = contactNumbers;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
