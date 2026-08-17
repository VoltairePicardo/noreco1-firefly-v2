package com.noreco1.fireflyv2.model;

import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.DateHelper;
import com.noreco1.fireflyv2.common.helpers.StringFormatter;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.model.enums.Address;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String employeeNumber;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String middleName = "";

    @Column
    private String suffixName = "";

    @Column
    private Date birthDate;

    @Column
    private String birthPlace;

    @Column
    private String bloodType;

    @Column
    private String nationality;

    @Column
    private String religion;

    @Column
    private Date employmentDate;

    @Column
    private Date resignationDate;

    @Column
    private Date retiredDate;

    @Column
    private String phicNumber;

    @Column
    private String sssNumber;

    @Column
    private String hdmfNumber;

    @Column
    private String tin;

    @Column
    private String houseNumber;

    @Column
    private String street;

    @Column
    private String barangay;

    @Column
    private String city;

    @Column
    private String contactNumber;

    @Column
    private String email;

    @Column
    private Integer step;

    @Column
    private Boolean qualified;

    @Column
    private String province;

    @Column
    private String country;
    @Column
    private String mailingHouseNumber;

    @Column
    private String mailingStreet;

    @Column
    private String mailingBarangay;

    @Column
    private String mailingCity;

    @Column
    private String mailingProvince;

    @Column
    private String mailingZipCode;

    @Column
    private String mailingCountry;
    @Column
    private String provincialHouseNumber;

    @Column
    private String provincialStreet;

    @Column
    private String provincialBarangay;

    @Column
    private String provincialCity;

    @Column
    private String provincialProvince;

    @Column
    private String provincialCountry;

    @Column
    private String provincialZipCode;

    @Column(name = "FK_accountNo")
    private Integer accountNumber;

    @Column
    private String zipCode;

    @Column(name = "`rank`")
    private String rank;

    @Column
    private Boolean isActive;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_createdByUserId", nullable = true, columnDefinition = "0")
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_signFileId")
    private FileUpload signature;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_officeId", nullable = true, columnDefinition = "0")
    private Office office;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_departmentId", nullable = true, columnDefinition = "0")
    private Department department;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_divisionId", nullable = true, columnDefinition = "0")
    private Division division;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_sectionId", nullable = true, columnDefinition = "0")
    private Section section;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_positionId", nullable = true, columnDefinition = "0")
    private Position position;

    public String getName() {
        StringBuilder builder = new StringBuilder();
        if (!Checker.isStringNullOrEmpty(this.firstName)) builder.append(this.firstName + " ");
        if (!Checker.isStringNullOrEmpty(this.middleName)) builder.append(this.middleName.charAt(0) + ". ");
        if (!Checker.isStringNullOrEmpty(this.lastName)) builder.append(this.lastName + " ");
        if (!Checker.isStringNullOrEmpty(this.suffixName)) builder.append(this.suffixName);

        return builder.toString();
    }

    public Integer getAge() {
        if (this.birthDate == null) return 0;
        return DateHelper.getAge(this.birthDate);
    }

    public String getAddress(String addressType) {
        return "";
    }

    public String getAddress(Address addressType) {
        String hn = StringFormatter.getStrElseBlank(this.houseNumber);
        String street = StringFormatter.getStrElseBlank(this.street);
        String barangay = StringFormatter.getStrElseBlank(this.barangay);
        String city = StringFormatter.getStrElseBlank(this.city);
        String zipCode = StringFormatter.getStrElseBlank(this.zipCode);
        String province = StringFormatter.getStrElseBlank(this.province);

        if (addressType == Address.MAILING) {
            hn = StringFormatter.getStrElseBlank(this.mailingHouseNumber);
            street = StringFormatter.getStrElseBlank(this.mailingStreet);
            barangay = StringFormatter.getStrElseBlank(this.mailingBarangay);
            city = StringFormatter.getStrElseBlank(this.mailingCity);
            zipCode = StringFormatter.getStrElseBlank(this.mailingZipCode);
            province = StringFormatter.getStrElseBlank(this.mailingProvince);
        } else if (addressType == Address.PROVINCIAL) {
            hn = StringFormatter.getStrElseBlank(this.provincialHouseNumber);
            street = StringFormatter.getStrElseBlank(this.provincialStreet);
            barangay = StringFormatter.getStrElseBlank(this.provincialBarangay);
            city = StringFormatter.getStrElseBlank(this.provincialCity);
            zipCode = StringFormatter.getStrElseBlank(this.provincialZipCode);
            province = StringFormatter.getStrElseBlank(this.provincialProvince);
        }

        String address = hn + " " + street + " " + barangay + " " + city + " " + province + " " + zipCode;
        return address.trim();
    }

}
