package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryBatch implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private Date date;

    @Column
    private Boolean voucherCreated;

    @ManyToOne
    @JoinColumn(name="FK_documentTypeId")
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_transactionId")
    private Transaction transaction;

    @Column
    private String remarks;

    public TemporaryBatch(Date date, Boolean voucherCreated, DocumentType documentType, Transaction transaction,
                          String remarks) {
        this.date = date;
        this.voucherCreated = voucherCreated;
        this.documentType = documentType;
        this.documentType = documentType;
        this.transaction = transaction;
        this.remarks = remarks;
    }

}
