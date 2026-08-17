package com.noreco1.fireflyv2.model;

import lombok.*;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DocumentWorkflowActionMap implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "FK_workflowId")
    private Workflow workflow;

    @ManyToOne
    @JoinColumn(name = "FK_workflowActionId")
    private WorkflowAction workflowAction;

    @Column
    private Integer sequence;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="FK_beforeActionDocumentStatusId")
    private DocumentStatus beforeActionDocumentStatus;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="FK_afterActionDocumentStatusId")
    private DocumentStatus afterActionDocumentStatus;

    @Column
    private String propSignatureType;

}
