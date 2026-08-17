import {Component, input} from '@angular/core';

@Component({
    selector: 'app-database-error',
    imports: [],
    templateUrl: './database-error.component.html',
    styleUrl: './database-error.component.scss'
})
export class DatabaseErrorComponent {
    message = input<string>('');
}
