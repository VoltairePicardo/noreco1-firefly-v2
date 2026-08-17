package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssetDepreciationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_assetDepreciationId")
    private AssetDepreciation assetDepreciation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_assetDepreciationScheduleDetailId")
    private AssetDepreciationScheduleDetail assetDepreciationScheduleDetail;

    @Column
    private BigDecimal depreciationAmount;

    @Column
    private BigDecimal depreciatedValue;

    @Column
    private BigDecimal remainingValue;

}
