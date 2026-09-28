import { appName, appNameExtended, credits, startYear } from '@/app/constants';
import { Component } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

@Component({
    selector: 'app-error-403',
    imports: [NgOptimizedImage],
    templateUrl: './error-403.component.html',
    styles: ``
})
export class Error403Component {
    startYear = startYear;
    credits = credits;
    appName = appName;
    appNameExtended = appNameExtended;
}
