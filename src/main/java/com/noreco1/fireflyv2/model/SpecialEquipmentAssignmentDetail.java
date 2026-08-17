package com.noreco1.fireflyv2.model;

import lombok.*;

import com.noreco1.fireflyv2.mysql_model.Consumer;
import com.noreco1.fireflyv2.mysql_model.TurnOnOrder;
import com.noreco1.fireflyv2.mysql_model.TurnOnOrderWithdrawal;
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
public class SpecialEquipmentAssignmentDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_specialEquipmentAssignmentId")
    private SpecialEquipmentAssignment specialEquipmentAssignment;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_specialEquipmentId")
    private SpecialEquipment specialEquipment;

    @Column(name="FK_turnOnOrderId")
    private Integer turnOnOrderId;

    @Transient
    private TurnOnOrder turnOnOrder;

    @Transient
    private boolean hasAccomplishment;

    @Transient
    private BigDecimal initialReading;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_stockTransactionDetailId")
    private StockTransactionDetail stockTransactionDetail;

    @Column(name="FK_consumerId")
    private Integer consumerId;

    @Transient
    private Consumer consumer;

    @Transient
    private String description;

    @Transient
    private String serialNo;

    @Transient
    private Integer itemId;

}
