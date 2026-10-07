package com.noreco1.fireflyv2.controller.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventorySubCategoryDto {

    private Integer id;
    private String description;
    private Integer categoryId;
    private String categoryDescription;
}
