package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class CostEstimateAssemblyUnit implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @JsonIgnore
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_costEstimateId", nullable = true, columnDefinition = "0")
    private CostEstimate costEstimate;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_assemblyUnitId", nullable = true, columnDefinition = "0")
    private AssemblyUnit assemblyUnit;

    @Column
    private BigDecimal quantity;

    @Column
    private BigDecimal unitCost;

    @Column
    private BigDecimal totalCost;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<CostEstimateAssemblyUnitItem> details = new ArrayList<>();

}
