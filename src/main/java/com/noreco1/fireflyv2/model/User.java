package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.validation.constraints.Email;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;
    
    @Column
    private String fullName;

    @NotNull
    @Length(min = 3, max = 64, message = "Invalid length for username (max=64, min=3)")
    @Column(unique = true)
    private String username;

    @NotNull
    @Email
    @Length(min = 10, max = 128, message = "Email length is invalid")
    @Column(unique = true)
    private String email;

    @Transient
    @Column
    private String password;

    @Transient
    @Column
    private String retypePassword;

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

    @Column(nullable = false)
    private Boolean enabled = false;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "FK_accountNo")
    private Integer accountNo;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_signFileId", columnDefinition = "0")
    private FileUpload signature;

    @Transient
    private String lastName;

    @Transient
    private String firstName;

    @Transient
    private String middleName;

    @Transient
    private String extensionName;

    @Transient
    private Department department;

    @Transient
    private Division division;

    @Transient
    private Section section;

    @Transient
    private Position position;

    public User(String fullName, String username, String email, String password, String retypePassword, User createdBy,
                Date createdAt, Date updatedAt, Boolean enabled, Set<Role> roles, Integer accountNo,
                FileUpload signature) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.retypePassword = retypePassword;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.enabled = enabled;
        this.roles = roles;
        this.accountNo = accountNo;
        this.signature = signature;
    }

}
