package com.noreco1.fireflyv2.model.enums;

import lombok.*;

@Getter
public enum InventoryDocType {

    MCT, // Material Requisition and charge ticket
    SA, // Stock Adjustment
    MCRT, // Material Credit Ticket
    MST, // Material Salvage Ticket
    STR, // Stock Transfer Receiving

}
