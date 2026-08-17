package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import jakarta.validation.constraints.Size;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @NotNull
    @Length(min = 3, max = 512, message = "Invalid length for username (max=512, min=3)")
    @Column(unique = true)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<Menu> menus = new ArrayList<>();

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<PageComponent> pageComponents = new ArrayList<>();

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<PageComponent> pageComponentsToEvict = new ArrayList<>();

    @Transient
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private ArrayList<Menu> menusToEvict = new ArrayList<>();

    public Role(String name, Date createdAt, Date updatedAt, ArrayList<Menu> menus, ArrayList<PageComponent> pageComponents,
                ArrayList<PageComponent> pageComponentsToEvict, ArrayList<Menu> menusToEvict) {
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.menus = menus;
        this.pageComponents = pageComponents;
        this.pageComponentsToEvict = pageComponentsToEvict;
        this.menusToEvict = menusToEvict;
    }

    public Role(String name, Date createdAt, Date updatedAt) {
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Role(String name) {
        this.name = name;
    }

}
