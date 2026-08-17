package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.noreco1.fireflyv2.controller.response.DocInqListDto;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.*;
import java.io.Serializable;
import java.lang.reflect.AnnotatedArrayType;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class BudgetLineItemDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_budgetLineItemId")
    private BudgetLineItem budgetLineItem;

    @Column
    private String title;

    @Column
    private BigDecimal length;

    @Column
    private String location;

    @Column
    private String justification;

    @Column
    private BigDecimal quantity;

    @Column
    private BigDecimal totalPrice;

    @Column
    private String code;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_fundingSourceId")
    private FundingSource fundingSource;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_budgetItemClassificationId")
    private BudgetItemClassification budgetItemClassification;

    @Column
    private String remarks;

    @Column
    private String specification;

    @Column
    private String balanceScoreCard;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_deliveryDateId")
    private DeliveryDate deliveryDate;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_budgetTypeId")
    private BudgetType budgetType;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_projectTypeId")
    private ProjectTypeBudgetLineItem projectType;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_itemId")
    private Item item;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_unitId")
    private UnitMeasure unit;

    @Column
    private Date expectedDeliveryDate;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_cashflowItemId")
    private CashflowItem cashflowItem;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_generalClassificationId")
    private GeneralClassification generalClassification;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_strategicInitiativeId")
    private StrategicInitiative strategicInitiative;

    @Column
    private Integer year;

    @Column
    private Integer startMonth;

    @Column
    private Integer endMonth;

    @Column
    private BigDecimal applicationAmount;

    @Column
    private BigDecimal nEAApprovedAmount;

    @Column
    private BigDecimal supplementalAmount;

    @Column
    private BigDecimal finalAmount;

    @Transient
    private BigDecimal budgetAmountBalancePOJORFP;

    @Transient
    private BigDecimal budgetAmountBalanceCV;

    @Transient
    private Boolean hasSubItems;

    @Transient
    private List<DocInqListDto> linkedDocuments = new ArrayList<>();

    @Transient
    private BigDecimal percentCompletion = BigDecimal.ZERO;

    @Transient
    private boolean completed = false;

    @JsonSetter("year")
    public void setYear(Object year) {
        if (year instanceof Integer) {
            this.year = (Integer) year;
        } else if (year instanceof Map) {
            Map<?, ?> yearMap = (Map<?, ?>) year;
            if (yearMap.containsKey("id") && yearMap.get("id") instanceof Integer) {
                this.year = (Integer) yearMap.get("id");
            } else if (yearMap.containsKey("description") && yearMap.get("description") instanceof Integer) {
                this.year = (Integer) yearMap.get("description");
            } else {
                this.year = null; // or a default value if neither field is found
            }
        }
    }

    @JsonSetter("startMonth")
    public void setStartMonth(Object startMonth) {
        this.startMonth = extractIntegerValue(startMonth);
    }

    @JsonSetter("endMonth")
    public void setEndMonth(Object endMonth) {
        this.endMonth = extractIntegerValue(endMonth);
    }

    private Integer extractIntegerValue(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            if (map.containsKey("id") && map.get("id") instanceof Integer) {
                return (Integer) map.get("id") + 1;
            } else if (map.containsKey("description") && map.get("description") instanceof Integer) {
                return (Integer) map.get("description");
            }
        }
        return null; // or a default value if needed
    }

}
