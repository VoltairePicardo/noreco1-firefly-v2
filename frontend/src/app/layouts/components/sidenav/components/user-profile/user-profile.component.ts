import {Component, CUSTOM_ELEMENTS_SCHEMA, OnInit} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {NgbCollapseModule, NgbDropdown, NgbDropdownMenu, NgbDropdownToggle} from '@ng-bootstrap/ng-bootstrap';
import {CommonModule} from '@angular/common';
import {NgIcon} from '@ng-icons/core';
import {userDropdownItems} from '@layouts/components/data';
import {AuthService} from '@/app/pages/auth/auth.service';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    RouterLink,
    NgbCollapseModule,
    NgbDropdown,
    NgbDropdownToggle,
    NgbDropdownMenu,
    CommonModule,
    NgIcon
  ],

  templateUrl: './user-profile.component.html',
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class UserProfileComponent implements OnInit {
    user: { fullName: string; position: string } = { fullName: '', position: '' };
    menuItems = userDropdownItems;

    constructor(public authService: AuthService, private router: Router) {}

    ngOnInit() {
        const currentUser = this.authService.getUser();
        this.user = {
            fullName: currentUser.fullName,
            position: currentUser.position,
        };
    }

    menuClicked(itemLabel: string | undefined) {
        if (itemLabel === 'Log Out') {
            this.authService.logout();
        } else if (itemLabel === 'Profile') {
            this.router.navigate(['/profile']);
        }
    }
}
