package com.noreco1.fireflyv2.controller.response;

/**
 * Created by lenovo on 11/25/2015.
 */
public class SectionDto {

    private int id;
    private String name;
    private int divisionId;
    private String divisionName;

    public SectionDto() {
    }

    public SectionDto(int id, String name, int divisionId, String divisionName) {
        this.id = id;
        this.name = name;
        this.divisionId = divisionId;
        this.divisionName = divisionName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(int divisionId) {
        this.divisionId = divisionId;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }
}
