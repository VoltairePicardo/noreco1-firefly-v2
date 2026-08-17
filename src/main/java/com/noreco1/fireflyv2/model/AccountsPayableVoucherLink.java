package com.noreco1.fireflyv2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsPayableVoucherLink implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "FK_accountsPayableVoucherId")
    private AccountsPayableVoucher accountsPayableVoucher;

    @ManyToOne
    @JoinColumn(name = "FK_documentTypeId")
    private DocumentType documentType;

    @Column(name = "FK_linkedDocumentId")
    private Integer documentId;
}
