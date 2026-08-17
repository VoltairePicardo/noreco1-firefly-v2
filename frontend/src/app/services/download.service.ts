import { Injectable } from '@angular/core';
import { TokenStorageService } from '@core/services/token-storage.service';
import { environment } from '@/environments/environment';
// import * as queryString from "querystring";

@Injectable({
  providedIn: 'root'
})
export class DownloadService {

  token: string | null = null;
  private baseUrl: string = environment.get('baseUrl') ?? '';

  constructor(private tokenStorageService: TokenStorageService) {
    this.token = this.tokenStorageService.getToken() ?? ''
  }

  print(url: string, params = {}) {
    url = this.baseUrl + url + '?token='+this.token;

    // let qs = queryString.stringify(params);
    const qs = new URLSearchParams(params).toString();
    url = url + '&' + qs;

    let newTab = window.open(url, '_blank');
    newTab!.focus();
  }
}
