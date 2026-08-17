package com.noreco1.fireflyv2.model;

import lombok.*;

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
@NoArgsConstructor
@AllArgsConstructor
public class ItemTestingDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_itemTestingId")
    private ItemTesting itemTesting;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_itemId")
    private Item item;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_poDetailId")
    private PoDetail poDetail;

    @Column
    private BigDecimal quantity;

    @Column
    private BigDecimal unitsReceivedQuantity;

    @Column
    private BigDecimal unitsRejectedQuantity;

    @Column
    private String remarks;

    @Transient
    private BigDecimal balance;

    @Transient
    private String itemCode;

    @Transient
    private String unitCode;

    @Transient
    private String itemDescription;

    @Transient
    private BigDecimal quantityReceived;

    @Transient
    private BigDecimal deliveredQuantity;

}
