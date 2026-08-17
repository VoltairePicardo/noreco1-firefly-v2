package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssetDepreciationScheduleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "FK_assetDepreciationScheduleId")
    private AssetDepreciationSchedule depreciationSchedule;

    @ManyToOne
    @JoinColumn(name = "FK_assetDetailId")
    private AssetDetail  assetDetail;

    @ManyToOne
    @JoinColumn(name = "FK_assetAccountId")
    private Account assetAccount;

    @Column
    private BigDecimal depreciationAmount = BigDecimal.ZERO;


}
