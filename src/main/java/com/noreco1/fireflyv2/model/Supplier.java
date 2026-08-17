package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.validation.constraints.Size;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Supplier implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotNull
    @Length(min = 3, max = 512, message = "Invalid length for full name (max=512, min=3)")
    @Column
    private String name;

    @Column
    private String address;

    @Column
    private String phone;

    @Column
    private String fax;

    @Column
    private String contactPerson;

    @Column
    private String contactPersonPosition;

    @Column(name = "FK_accountNo")
    private Integer accountNumber;

    @Column
    private String email;

    @Column
    private String tin;

    @Column
    private Boolean vatable;

    @Column
    private String bankAccountNumber;

    @Column
    private Boolean status;

    @Column
    private BigDecimal creditLimit;

    @Column
    private String zip;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_createdByUserId", nullable = true, columnDefinition = "0")
    private User createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_SLEntityClassificationId", nullable = false)
    private SLEntityClassification slEntityClassification;

    @Column
    private String remarks;

    @Column
    private Boolean accredited;

    public Supplier(String name, String address, String phone, String fax, String contactPerson, String contactPersonPosition,
                    Integer accountNumber, String email, String tin, Boolean vatable, String bankAccountNumber, Boolean status,
                    BigDecimal creditLimit, String zip, User createdBy, Date createdAt, Date updatedAt,
                    SLEntityClassification slEntityClassification, String remarks, Boolean accredited) {

        this.accredited = accredited;
        this.remarks = remarks;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.fax = fax;
        this.contactPerson = contactPerson;
        this.contactPersonPosition = contactPersonPosition;
        this.accountNumber = accountNumber;
        this.email = email;
        this.tin = tin;
        this.vatable = vatable;
        this.bankAccountNumber = bankAccountNumber;
        this.status = status;
        this.creditLimit = creditLimit;
        this.zip = zip;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.slEntityClassification = slEntityClassification;
    }

    public Boolean isVatable() {
        if(this.vatable == null) return false;
        return vatable;
    }

    public void isVatable(Boolean vatable) {
        this.vatable = vatable;
    }

    public void isAccredited(Boolean accredited) {
        accredited = accredited;
    }
}
