package com.noreco1.fireflyv2.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubLedger extends Ledger implements Serializable {

    @ManyToOne
    @JoinColumn(name = "FK_accountNo")
    private SlEntity slEntity;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name = "FK_generalLedgerLineId", nullable = true)
    private GeneralLedger generalLedger;
}
