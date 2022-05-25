import { Inject, Injectable, Optional } from '@angular/core';

import { saveAs } from 'file-saver';

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { LoginService } from 'app/core/login.service';
import { SERVER_URL } from 'app/core/variables';

import { AnalyticsService } from 'app/core//analytics.service';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SaveAsService {
  private serverURL!: string;

  constructor(
    private loginService: LoginService,
    private httpClient: HttpClient,
    private analyticsService: AnalyticsService,
    @Optional()
    @Inject(SERVER_URL)
    serverURL: string
  ) {
    if (serverURL) {
      this.serverURL = serverURL;
    }
  }

  public saveAs(id: string, name: string) {
    const fileDownloadURL = `${this.serverURL}rest/download/${id}`;
    const xhr = new XMLHttpRequest();
    xhr.open('GET', fileDownloadURL, true);
    // Manually set the authorization header, seems to work.
    xhr.setRequestHeader(
      'Authorization',
      `Basic ${btoa(this.loginService.getTicket())}`
    );
    xhr.responseType = 'blob';

    xhr.onload = (_e) => {
      saveAs(xhr.response, name);
    };
    xhr.send();
    this.analyticsService.trackDownload(fileDownloadURL);
  }

  public saveAsDirect(id: string, name: string) {
    const fileDownloadURL = `${
      this.serverURL
    }rest/download/${id}?ticket=${this.loginService.getTicket()}`;
    this.analyticsService.trackDownload(fileDownloadURL);
    this.download(fileDownloadURL, name);
  }
  public saveUrlAs(url: string, name: string) {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    // Manually set the authorization header, seems to work.
    xhr.setRequestHeader(
      'Authorization',
      `Basic ${btoa(this.loginService.getTicket())}`
    );
    xhr.responseType = 'blob';

    xhr.onload = (_e) => {
      saveAs(xhr.response, name);
    };
    xhr.send();
    this.analyticsService.trackDownload(url);
  }

  public async saveUrlAsync(url: string, name: string) {
    let headers = new HttpHeaders();
    // authentication (basicAuth) required
    headers = headers.set(
      'Authorization',
      `Basic ${btoa(this.loginService.getTicket())}`
    );

    const res = await firstValueFrom(
      this.httpClient.get<Blob>(url, {
        withCredentials: true,
        headers: headers,
        responseType: 'blob' as 'json',
      })
    );

    if (res) {
      saveAs(res, name);
    }
    this.analyticsService.trackDownload(url);
  }

  private download(url: string, fileName: string) {
    if (url && fileName) {
      const link = document.createElement('a');

      link.style.display = 'none';
      link.download = fileName;
      link.href = url;

      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }
}
