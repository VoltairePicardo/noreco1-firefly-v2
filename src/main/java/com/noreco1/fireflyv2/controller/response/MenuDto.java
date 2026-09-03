package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TSI Admin on 12/1/2014.
 */
public class MenuDto {

    private int id;
    private String state;
    private String title;
    private String iconClass;
    private String url;
    private Menu parentMenu;
    private List<MenuDto> subMenus = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public Menu getParentMenu() {
        return parentMenu;
    }

    public void setParentMenu(Menu parentMenu) {
        this.parentMenu = parentMenu;
    }

    public List<MenuDto> getSubMenus() {
        return subMenus;
    }

    public void setSubMenus(List<MenuDto> subMenus) {
        this.subMenus = subMenus;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
