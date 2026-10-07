package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.InventoryCategoryType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InventoryCategoryDto {

    private Integer id;
    private String description;
    private InventoryCategoryType type;
    private List<SubCategoryDto> subCategories = new ArrayList<>();

    @Getter
    @Setter
    public static class SubCategoryDto {
        private Integer id;
        private String description;
    }
}
