package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MaterialIssueRegister extends Voucher implements Serializable {

    @NotNull(message = "Please enter particulars.")
    @Column
    private String particulars;

    @Column
    private String inventoryDocType;

    @Column
    private Integer invDocTransactionId;

    @Transient
    private List<MaterialIssueRegisterDetail> materialIssueRegisterDetails;

    @NotNull(message = "Please select recommending officer.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_recommendedByUserId")
    private User recommendingOfficer;

}
