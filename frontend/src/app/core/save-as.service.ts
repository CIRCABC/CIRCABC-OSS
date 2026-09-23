import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { AnalyticsService } from 'app/core//analytics.service';
import { LoginService } from 'app/core/login.service';
import { SERVER_URL } from 'app/core/variables';
import { saveAs } from 'file-saver';
import { firstValueFrom } from 'rxjs';

/**
 * Application-wide service responsible for downloading files from the CIRCABC
 * backend and saving them to the user's device.
 *
 * It offers several download strategies (authenticated XHR requests, direct
 * ticket-based links and an async `HttpClient`-based approach) and delegates
 * the actual "save file" step to the {@link https://github.com/eligrey/FileSaver.js | file-saver}
 * library. Every download is reported to the {@link AnalyticsService} for
 * tracking.
 *
 * Key collaborators:
 * - {@link LoginService} — provides the authentication ticket used to build
 *   the `Basic` authorization header / query parameter.
 * - {@link HttpClient} — used by the async download variant.
 * - {@link AnalyticsService} — records each download event.
 * - `SERVER_URL` — injection token holding the backend base URL.
 *
 * Registered as a root-level singleton (`providedIn: 'root'`).
 */
@Service()
export class SaveAsService {
  /** Provides the current session ticket used to authenticate download requests. */
  private readonly loginService = inject(LoginService);
  /** Angular HTTP client used by the async download variant. */
  private readonly httpClient = inject(HttpClient);
  /** Records download events for analytics tracking. */
  private readonly analyticsService = inject(AnalyticsService);

  /** Base URL of the CIRCABC backend, injected via the `SERVER_URL` token. */
  private readonly serverURL = inject(SERVER_URL);

  /**
   * Downloads a node's content by its identifier using an authenticated
   * `XMLHttpRequest` and saves it locally under the given file name.
   *
   * The request URL is built from the configured server URL and the node id,
   * and the authorization header is set manually from the login ticket. The
   * download is also reported to the analytics service.
   *
   * @param id - Identifier of the node whose content should be downloaded.
   * @param name - File name to use when saving the downloaded blob.
   */
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
    this.analyticsService.trackDownload(fileDownloadURL, name);
  }

  /**
   * Downloads a node's content by its identifier using a direct browser link
   * that carries the login ticket as a query parameter.
   *
   * Reports the download to the analytics service and triggers the download
   * via a temporary anchor element (see {@link download}).
   *
   * @param id - Identifier of the node whose content should be downloaded.
   * @param name - File name to use when saving the downloaded file.
   */
  public saveAsDirect(id: string, name: string) {
    const fileDownloadURL = `${
      this.serverURL
    }rest/download/${id}?ticket=${this.loginService.getTicket()}`;

    this.analyticsService.trackDownload(fileDownloadURL, name);
    this.download(fileDownloadURL, name);
  }

  /**
   * Downloads the content located at an arbitrary URL using an authenticated
   * `XMLHttpRequest` and saves it locally under the given file name.
   *
   * The authorization header is set manually from the login ticket, and the
   * download is reported to the analytics service.
   *
   * @param url - Absolute URL of the resource to download.
   * @param name - File name to use when saving the downloaded blob.
   */
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
    this.analyticsService.trackDownload(url, name);
  }

  /**
   * Asynchronously downloads the content located at an arbitrary URL using
   * Angular's {@link HttpClient} and saves it locally under the given file name.
   *
   * Uses basic authentication built from the login ticket and requests the
   * response as a blob. If a response body is received it is saved via
   * file-saver. The download is reported to the analytics service.
   *
   * @param url - Absolute URL of the resource to download.
   * @param name - File name to use when saving the downloaded blob.
   * @returns A promise that resolves once the request has completed and the
   * file has been saved (when a response body is present).
   */
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
    this.analyticsService.trackDownload(url, name);
  }

  /**
   * Triggers a browser download for the given URL by creating a hidden anchor
   * element, programmatically clicking it and then removing it from the DOM.
   *
   * Does nothing if either argument is falsy.
   *
   * @param url - Absolute URL of the resource to download.
   * @param fileName - File name suggested to the browser via the `download`
   * attribute.
   */
  private download(url: string, fileName: string) {
    if (url && fileName) {
      const link = document.createElement('a');

      link.style.display = 'none';
      link.download = fileName;
      link.href = url;

      document.body.appendChild(link);
      link.click();
      link.remove();
    }
  }
}
