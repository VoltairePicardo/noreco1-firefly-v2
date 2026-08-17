package com.noreco1.fireflyv2.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryGeneralLedger extends Ledger implements Serializable {

    @Transient
    private ArrayList<Object> subLedgerLines = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_temporaryBatchId")
    private TemporaryBatch temporaryBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_accountId", nullable = true, columnDefinition = "0")
    private Account account;
}
