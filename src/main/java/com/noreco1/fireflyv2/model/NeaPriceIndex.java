package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class NeaPriceIndex implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Date effectivityDate;

    @Column
    private String description;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_encodedByUserId")
    private User encodedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @Transient
    private List<NeaPriceIndexDetail> neaPriceIndexDetails = new ArrayList<>();

    public NeaPriceIndex(Date effectivityDate, String description, User encodedBy, Date createdAt, Date updatedAt) {
        this.effectivityDate = effectivityDate;
        this.description = description;
        this.encodedBy = encodedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
