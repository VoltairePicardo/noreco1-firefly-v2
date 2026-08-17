import {MenuItemType} from '@/app/types/layout';

type UserDropdownItemType = {
    label?: string;
    icon?: string;
    url?: string;
    isDivider?: boolean;
    isHeader?: boolean;
    class?: string;
}

export const userDropdownItems: UserDropdownItemType[] = [
    {
        label: 'Welcome back!',
        isHeader: true
    },
    {
        label: 'Profile',
        icon: 'tablerUserCircle',
        url: '#'
    },
    {
        label: 'Log Out',
        icon: 'tablerLogout2',
        url: '#',
        class: 'text-danger fw-semibold'
    }
];


export const horizontalMenuItems: MenuItemType[] = [];
