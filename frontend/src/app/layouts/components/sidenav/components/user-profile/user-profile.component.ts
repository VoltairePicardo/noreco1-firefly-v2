import { Component, OnInit } from '@angular/core';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '@/app/pages/auth/auth.service';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [NgbTooltipModule],
  templateUrl: './user-profile.component.html',
})
export class UserProfileComponent implements OnInit {
    user: { fullName: string; position: string } = { fullName: '', position: '' };

    constructor(private authService: AuthService) {}

    ngOnInit() {
        const currentUser = this.authService.getUser();
        this.user = {
            fullName: currentUser?.fullName ?? '',
            position: currentUser?.position ?? '',
        };
    }
}
