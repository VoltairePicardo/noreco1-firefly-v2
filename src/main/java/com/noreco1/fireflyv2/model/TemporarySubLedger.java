package com.noreco1.fireflyv2.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TemporarySubLedger extends Ledger implements Serializable {

    @ManyToOne
    @JoinColumn(name = "FK_accountNo")
    private SlEntity slEntity;

    @ManyToOne
    @JoinColumn(name = "FK_temporaryGeneralLedger", nullable = true)
    private TemporaryGeneralLedger temporaryGeneralLedger;

    @Column
    private BigDecimal balance;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_temporaryBatchId", nullable = true)
    private TemporaryBatch temporaryBatch;
}
