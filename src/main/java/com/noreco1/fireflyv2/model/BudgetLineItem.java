package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class BudgetLineItem extends Document implements Serializable {

    @Column
    private Integer year;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_departmentId")
    private Department department;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_divisionId")
    private Division division;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_checkedByUserId")
    private User checkedBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_verifiedByUserId")
    private User verifiedBy;

    @Column(name = "isForSupplementalBudget")
    private Boolean forSupplementalBudget;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<BudgetLineItemDetail> budgetLineItemDetails = new ArrayList<>();

    @Transient
    private Boolean isForAddingAdditionalDetails = Boolean.FALSE;

    @Transient
    private Boolean isForApproval = Boolean.FALSE;

    @Transient
    private Boolean isSelected = Boolean.FALSE;


}
