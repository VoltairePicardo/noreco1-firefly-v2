package com.noreco1.fireflyv2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String code;

    @Size(
            min = 3,
            max = 500,
            message = "The title '${validatedValue}' must be between {min} and {max} characters long"
    )
    @Column(unique = true)
    private String title;

    @JsonProperty("glaccount")
    @Column
    private String GLAccount;

    @JsonProperty("slaccount")
    @Column
    private String SLAccount;

    @JsonProperty("auxAccount")
    @Column
    private String auxiliaryAccount;

    @Column
    private int normalBalance;

    @Column
    private Integer level;

    @JsonProperty("isActive")
    @Column
    private int active;

    @Column
    private int isHeader;

    @Column
    private int hasSL;

    @Column
    private Integer parentAccountId;

    @Transient
    private Set<SegmentAccount> segmentAccounts = new HashSet<>();

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_accountTypeId")
    private AccountType accountType;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_accountGroupId")
    private AccountGroup accountGroup;

    @Column
    private String classification;

    @Transient
    private Account parentAccount;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_bsupAccountId")
    private Account bsupAccount;

    @Transient
    private Factor allocationFactor;

    @Override
    public String toString() {
        return this.getTitle();
    }
}
