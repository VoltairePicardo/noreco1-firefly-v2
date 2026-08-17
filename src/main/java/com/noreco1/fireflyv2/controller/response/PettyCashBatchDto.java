package com.noreco1.fireflyv2.controller.response;

/**
 * Created by TSI Admin.
 */
public class PettyCashBatchDto {
    private Integer id;
    private Integer status;

    public PettyCashBatchDto() {
    }

    public PettyCashBatchDto(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
