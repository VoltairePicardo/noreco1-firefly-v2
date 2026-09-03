import {Component} from '@angular/core';
import {CommonModule} from "@angular/common";
import {NgbDropdown, NgbDropdownMenu, NgbDropdownToggle} from "@ng-bootstrap/ng-bootstrap";
import {userDropdownItems} from '@layouts/components/data';
import {Router, RouterLink} from '@angular/router';
import {NgIcon} from '@ng-icons/core';
import {AuthService} from '@/app/pages/auth/auth.service';

@Component({
  selector: 'app-user-profile-topbar',
  standalone: true,
  imports: [
    CommonModule,
    NgbDropdown,
    NgbDropdownMenu,
    NgbDropdownToggle,
    RouterLink,
    NgIcon
  ],
  templateUrl: './user-profile.component.html'
})
export class UserProfileComponent {
    user: string = '';
    menuItems = userDropdownItems;

    constructor(public authService: AuthService, private router: Router) {}

    ngOnInit() {
        const currentUser = this.authService.getUser();
        this.user = currentUser.fullName;
    }

    menuClicked(itemLabel: string | undefined) {
        if (itemLabel === 'Log Out') {
            this.authService.logout();
        } else if (itemLabel === 'Profile') {
            this.router.navigate(['/profile']);
        }
    }
}
