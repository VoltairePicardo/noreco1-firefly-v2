package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String username;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date attemptDateTime;

    @Column
    private String ip;

    @Column
    private Boolean successful;

    @Column
    private Boolean isMaster;

    public Login(String username, Date attemptDateTime, String ip, Boolean successful, Boolean isMaster) {
        this.username = username;
        this.attemptDateTime = attemptDateTime;
        this.ip = ip;
        this.successful = successful;
        this.isMaster = isMaster;
    }

}
