package com.noreco1.fireflyv2.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SegmentAccount implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @Column
    private String accountCode;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_accountId")
    private Account account;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_businessSegmentId")
    private BusinessSegment businessSegment;

    @Override
    public String toString() {
        String code = (this.getAccountCode() != null ? this.getAccountCode() : "");
        String title = (this.getAccount() != null && this.getAccount().getTitle() != null ? this.getAccount().getTitle() : "");
        return code + " " + title;
    }
}
