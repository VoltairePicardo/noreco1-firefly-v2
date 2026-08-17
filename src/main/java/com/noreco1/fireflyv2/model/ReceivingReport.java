package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingReport extends Document implements Serializable {

    @NotNull(message = "Please select supplier.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_supplierId")
    private Supplier supplier;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_inventoryLocationId")
    private InventoryLocation inventoryLocation;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date deliveryDate;

    @Column
    private String deliveryNumber;

    @Column
    private BigDecimal totalAmount;

    @Column
    private BigDecimal totalQuantity;

    @Temporal(TemporalType.DATE)
    @Column
    private Date invoiceDate;

    @Column
    private String invoiceNumber;

    @Column
    private String remarks;

    @Column
    private Integer year;

    @Column
    private Boolean isRepairedItems;

    @Column
    private Boolean isJO;

    @Column
    private Boolean isRV;

    @Column
    private Boolean transactionType = false;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_checkedByUserId")
    private User checker;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<ReceivingReportDetail> rrDetails = new ArrayList<>();

    @Column
    private Boolean confirmedForJv;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_departmentId")
    private Department department;

    public Boolean getIsRepairedItems() {
        if(this.isRepairedItems == null) return false;
        return isRepairedItems;
    }

    public Boolean getIsJO() {
        if(this.isJO == null) return false;
        return isJO;
    }

    public Boolean getIsRV() {
        if(this.isRV == null) return false;
        return isRV;
    }

}
