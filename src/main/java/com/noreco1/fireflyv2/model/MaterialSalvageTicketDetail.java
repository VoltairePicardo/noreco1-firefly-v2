package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

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
public class MaterialSalvageTicketDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Integer matId;

    @Column
    private Integer quantity;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_materialSalvageTicketId", nullable = true, columnDefinition = "0")
    private MaterialSalvageTicket materialSalvageTicket;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
    private Item item;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_memorandumReceiptDetailId")
    private MemorandumReceiptDetail memorandumReceiptDetail;

    public MaterialSalvageTicketDetail(Integer matId, Integer quantity, MaterialSalvageTicket materialSalvageTicket, Item item, MemorandumReceiptDetail memorandumReceiptDetail) {
        this.matId = matId;
        this.quantity = quantity;
        this.materialSalvageTicket = materialSalvageTicket;
        this.item = item;
        this.memorandumReceiptDetail = memorandumReceiptDetail;
    }

}
