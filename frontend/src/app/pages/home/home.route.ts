import {Routes} from '@angular/router';
import {HomeComponent} from '@/app/pages/home/home/home.component';

const mainPath = '/home';
export const HOME_ROUTES: Routes = [
    { path: '', component: HomeComponent, data: {mainPath: mainPath} }
];
