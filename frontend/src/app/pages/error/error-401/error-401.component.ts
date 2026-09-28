import { appName, appNameExtended, credits, startYear } from '@/app/constants';
import { Component } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

@Component({
    selector: 'app-error-401',
    imports: [NgOptimizedImage],
    templateUrl: './error-401.component.html',
    styles: ``
})
export class Error401Component {
    startYear = startYear;
    credits = credits;
    appName = appName;
    appNameExtended = appNameExtended;
}
