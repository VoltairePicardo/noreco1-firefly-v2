package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class BillOfMaterialAssemblyUnitItem implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column
        private Integer id;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @NotFound(action = NotFoundAction.IGNORE)
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name="FK_billOfMaterialAssemblyUnitId", nullable = true, columnDefinition = "0")
        private BillOfMaterialAssemblyUnit billOfMaterialAssemblyUnit;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @NotFound(action = NotFoundAction.IGNORE)
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
        private Item item;

}
