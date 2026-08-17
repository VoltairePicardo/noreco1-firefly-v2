package com.noreco1.fireflyv2.model;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PageComponent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @Column
    private String html;

    @Column
    private String description;

    @Column
    private String clazz;

    @Column
    private String domId;

    @ManyToOne
    @JoinColumn(name = "FK_pageId")
    private Page page;

    @ManyToOne
    @JoinColumn(name = "FK_viewRouteId")
    private Route viewRoute;

    @ManyToOne
    @JoinColumn(name = "FK_actionRouteId")
    private Route actionRoute;

    public PageComponent(String html, String description, String clazz, String domId, Page page, Route viewRoute, Route actionRoute) {
        this.html = html;
        this.description = description;
        this.clazz = clazz;
        this.domId = domId;
        this.page = page;
        this.viewRoute = viewRoute;
        this.actionRoute = actionRoute;
    }

    @Override
    public String toString() {
        return this.getHtml();
    }
}
