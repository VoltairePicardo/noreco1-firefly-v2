package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class JunkMaterialsTicketDetail {

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
    @JoinColumn(name="FK_junkMaterialsTicketId", nullable = true, columnDefinition = "0")
    private JunkMaterialsTicket junkMaterialsTicket;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne
    @JoinColumn(name="FK_itemId", nullable = true, columnDefinition = "0")
    private Item item;

    public JunkMaterialsTicketDetail(Integer matId, Integer quantity, JunkMaterialsTicket junkMaterialsTicket, Item item) {
        this.matId = matId;
        this.quantity = quantity;
        this.junkMaterialsTicket = junkMaterialsTicket;
        this.item = item;
    }

}
