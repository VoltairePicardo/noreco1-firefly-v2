package com.noreco1.fireflyv2.controller.response;

import java.io.Serializable;

/**
 * Created by TSI Admin on 10/7/2014.
 */
public class CoaPrintDto implements Serializable {
    private String title;
    private String type;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.title;
    }
}
