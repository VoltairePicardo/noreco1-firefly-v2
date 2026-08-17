package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "FK_transactionId")
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "FK_fileId")
    private FileUpload file;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @Column()
    private String prefix;  // to differentiate files


}
