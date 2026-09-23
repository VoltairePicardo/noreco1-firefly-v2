package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String title;

    @Column
    private String state;

    @Column
    private String iconClass;

    @Column
    private String type;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_parentMenuId", nullable = true, columnDefinition = "0")
    private Menu parentMenu;

    @Column(name = "FK_viewRouteId")
    private Integer viewRouteId;

    @Column
    private String url;

    public Menu(Integer id, String title, String state, String iconClass, String type, String url) {}

}
