package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.SpecialEquipment;
import com.noreco1.fireflyv2.model.User;
import com.noreco1.fireflyv2.mysql_model.Consumer;

import java.util.Date;

/**
 * Created by Tri-Nvent on 8/17/2020.
 */
public class AssignSpecialEquipmentDto {

    private Integer id;
    private SpecialEquipment specialEquipment;
    private Date date;
    private Consumer consumer;
    private User createdBy;

    public AssignSpecialEquipmentDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SpecialEquipment getSpecialEquipment() {
        return specialEquipment;
    }

    public void setSpecialEquipment(SpecialEquipment specialEquipment) {
        this.specialEquipment = specialEquipment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

}
