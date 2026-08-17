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
@NoArgsConstructor
@AllArgsConstructor
public class JoAcceptanceDetail implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_joAcceptanceId")
    private JoAcceptance joAcceptance;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_joDetailId")
    private JoDetail joDetail;

    @Column
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column
    private BigDecimal vat = BigDecimal.ZERO;

    @Column
    private BigDecimal discount = BigDecimal.ZERO;

    @Column
    private BigDecimal amount = BigDecimal.ZERO;

    @Column
    private BigDecimal adjustment = BigDecimal.ZERO;

    @Column
    private BigDecimal netAmount = BigDecimal.ZERO;

    public JoAcceptanceDetail(JoDetail joDetail) {
        this.joDetail = joDetail;
    }

}

