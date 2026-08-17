package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentJournal extends Voucher {

    @NotNull(message = "Please enter explanation.")
    @Column
    private String explanation;

    @Column
    private String remarks;

    @NotNull(message = "Please select recommending officer.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_recommendedByUserId")
    private User recommendingOfficer;

    @NotNull(message = "Please select transaction type")
    @Column
    private String transactionType;

}
