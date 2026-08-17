package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class FileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column(length = 65535, columnDefinition="Text")
    private String filename;

    @Column(length = 65535, columnDefinition="Text")
    private String originalFilename;

    @Column
    private String mimeType;

}