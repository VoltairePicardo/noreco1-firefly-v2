import { appName, appNameExtended, credits, startYear } from '@/app/constants';
import { Component } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

@Component({
    selector: 'app-error-404',
    imports: [NgOptimizedImage],
    templateUrl: './error-404.component.html',
    styles: ``
})
export class Error404Component {
    startYear = startYear;
    credits = credits;
    appName = appName;
    appNameExtended = appNameExtended;
}
