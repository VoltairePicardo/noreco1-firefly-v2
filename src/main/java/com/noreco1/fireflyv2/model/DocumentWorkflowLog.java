package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DocumentWorkflowLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "FK_documentTransactionId")
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "FK_documentWorkflowActionMapId")
    private DocumentWorkflowActionMap documentWorkflowActionMap;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date executedAt;

}
