export interface InventoryLocation {
    id: number;
    description?: string;
    name?: string;
}

export interface InventoryCategory {
    id: number;
    description?: string;
    name?: string;
    type?: number;
}

export interface Purpose {
    id: number;
    description?: string;
    name?: string;
    type?: string;
}
