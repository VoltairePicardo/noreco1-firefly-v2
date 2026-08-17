package com.noreco1.fireflyv2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountType implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    @NotBlank
    private String description;

    @NotBlank
    @Column
    private String code;

    @Override
    public String toString() {
        return this.description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AccountType) {
            return id == (((AccountType) obj).id);
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
}
