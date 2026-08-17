package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class StockTransfer extends Document implements Serializable {

    @Column
    private String remarks;

    @Column
    private Date voucherDate;

    @Column
    private Integer year;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_fromInventoryLocationId", nullable = true, columnDefinition = "0")
    private InventoryLocation fromInventoryLocation;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_toInventoryLocationId", nullable = true, columnDefinition = "0")
    private InventoryLocation toInventoryLocation;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<ItemTransactionDetailDto> details = new ArrayList<>();

    public StockTransfer(String code, Transaction transaction, User approvingOfficer, Workflow workflow, Date createdAt, Date updatedAt, User createdBy, DocumentStatus documentStatus, String remarks, Date voucherDate, Integer year, InventoryLocation fromInventoryLocation, InventoryLocation toInventoryLocation, ArrayList<ItemTransactionDetailDto> details) {
        this.remarks = remarks;
        this.voucherDate = voucherDate;
        this.year = year;
        this.fromInventoryLocation = fromInventoryLocation;
        this.toInventoryLocation = toInventoryLocation;
        this.details = details;
    }

}
