package com.noreco1.fireflyv2.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank
    @Column
    private String code;

    @NotBlank
    @Column
    private String description;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_accountTypeId")
    private AccountType accountType;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AccountGroup) {
            return description.equalsIgnoreCase(((AccountGroup) obj).description);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + code.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
