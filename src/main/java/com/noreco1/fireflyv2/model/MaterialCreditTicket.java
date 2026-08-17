package com.noreco1.fireflyv2.model;

import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCreditTicket extends Document implements Serializable {

    @Column
    private String remarks;

    @Column
    private Date voucherDate;

    @Column
    private Integer year;

    @Column
    private BigDecimal amount;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_stockReleaseId", nullable = true, columnDefinition = "0")
    private StockRelease stockRelease;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_checkedByUserId", nullable = true, columnDefinition = "0")
    private User checker;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<ItemTransactionDetailDto> details = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_requestedByUserId", nullable = true, columnDefinition = "0")
    private User requester;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_inventoryLocationId", nullable = true, columnDefinition = "0")
    private InventoryLocation inventoryLocation;

}
