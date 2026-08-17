package com.noreco1.fireflyv2.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;
    
    @Column
    private String code;

    @NotEmpty
    @Column
    private String description;

    @Column(name = "Mat_Id")
    private Integer matId;

    @Column(name = "Mat_Code")
    private String matCode;

    @Column(name = "Mat_Desc")
    private String matDescription;

    @Column(name = "Mat_Units")
    private String matUnit;

    @Column(name = "a_Code")
    private String matACode;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_unitId")
    private UnitMeasure unit;

    @Column
    private BigDecimal reorderPoint;

    @Column
    private BigDecimal idealQty;

    @Column
    private String location;

    @Column
    private Boolean isActive;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_assetAccountId")
    private Account assetAccount;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_expenseAccountId")
    private Account expenseAccount;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_inventoryCategoryId")
    private InventoryCategory inventoryCategory;

    @Column
    private String fileName;

    @Column
    private Boolean hasSerialNumbers;

    @Column
    private String barcode;

    @Transient
    private String base64Image = "";

}
