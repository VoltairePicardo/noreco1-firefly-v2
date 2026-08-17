package com.noreco1.fireflyv2.controller.response;

/**
 * Created by TSI Admin on 12/1/2014.
 */
public class PageComponentDto {

    private int id;
    private int pageId;
    private int pageComponentId;
    private String description;
    private int viewRouteId;
    private int actionRouteId;

    public PageComponentDto() {}
    public PageComponentDto(int id, int pageId, int pageComponentId, String description, int viewRouteId, int actionRouteId) {
        this.id = id;
        this.pageId = pageId;
        this.pageComponentId = pageComponentId;
        this.description = description;
        this.viewRouteId = viewRouteId;
        this.actionRouteId = actionRouteId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getPageComponentId() {
        return pageComponentId;
    }

    public void setPageComponentId(int pageComponentId) {
        this.pageComponentId = pageComponentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getViewRouteId() {
        return viewRouteId;
    }

    public void setViewRouteId(int viewRouteId) {
        this.viewRouteId = viewRouteId;
    }

    public int getActionRouteId() {
        return actionRouteId;
    }

    public void setActionRouteId(int actionRouteId) {
        this.actionRouteId = actionRouteId;
    }

}
