package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum ProjectAttachmentPrefix {

    PLANNED_STAKING_SHEET("filePlannedStaking_"),
    BUILT_STAKING_SHEET("fileBuiltStaking_"),
    BUILT_OF_MATERIALS("fileBuiltMaterials_"),
    OTHER_DOCUMENTS("fileOtherDocs_"),
    ITEM_IMAGE("itemImage_");

    private String prefix;

    ProjectAttachmentPrefix(String prefix) {
        this.prefix = prefix;
    }

}
