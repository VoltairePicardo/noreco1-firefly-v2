export interface InventoryCategoryType {
    id: number;
    description: string;
}

export interface InventorySubCategory {
    id?: number | null;
    description: string;
    categoryId?: number | null;
    categoryDescription?: string;
}

export interface InventoryCategory {
    id?: number | null;
    description: string;
    type?: InventoryCategoryType | null;
    subCategories?: InventorySubCategory[];
}
