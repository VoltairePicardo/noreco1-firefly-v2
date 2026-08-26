import { User } from '@/app/models/user.model';

export interface Transaction {
    id: number;
    createdBy?: User;
    createdAt?: string;
}

export interface Workflow {
    id: number;
    name?: string;
    enabled?: boolean;
}

export interface DocumentStatus {
    id: number;
    status?: string;
}

export interface Office {
    id: number;
    name?: string;
    acronym?: string;
}

export interface Department {
    id: number;
    name?: string;
    abbreviation?: string;
}

export interface SpecialEquipment {
    id: number;
    serialNo?: string;
}

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
