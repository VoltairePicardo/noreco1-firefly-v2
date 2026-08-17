import { Injectable, inject } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

/**
 * NotificationService
 * 
 * Service for displaying toast notifications throughout the application.
 * Provides methods for success, error, info, and warning messages.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  // Inject ToastrService using inject() function
  private readonly toastr = inject(ToastrService);

  /**
   * Display a success notification
   */
  showSuccess(message: string, title: string): void {
    this.toastr.success(message, title);
  }

  /**
   * Display an error notification
   */
  showError(message: string, title: string): void {
    this.toastr.error(message, title);
  }

  /**
   * Display an info notification
   */
  showInfo(message: string, title: string): void {
    this.toastr.info(message, title);
  }

  /**
   * Display a warning notification
   */
  showWarning(message: string, title: string): void {
    this.toastr.warning(message, title);
  }
}