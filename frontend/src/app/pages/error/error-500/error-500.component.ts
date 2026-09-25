import { appName, appNameExtended, credits, startYear } from '@/app/constants';
import { Component } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

@Component({
    selector: 'app-error-500',
    imports: [NgOptimizedImage],
    templateUrl: './error-500.component.html',
    styles: ``
})
export class Error500Component {
    startYear = startYear;
    credits = credits;
    appName = appName;
    appNameExtended = appNameExtended;
}
