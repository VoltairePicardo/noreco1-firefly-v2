package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class PettyCashTransDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_pettyCashTransId", nullable = true, columnDefinition = "0")
    private PettyCashTrans pettyCashTrans;

    @Column
    private BigDecimal amount;

    @Column
    private String remarks;

    @Column
    private BigDecimal balance = BigDecimal.ZERO;

    public PettyCashTransDetail(PettyCashTrans pettyCashTrans, BigDecimal amount,
                                String remarks, BigDecimal balance) {
        this.pettyCashTrans = pettyCashTrans;
        this.amount = amount;
        this.remarks = remarks;
        this.balance = balance;
    }

}
