import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, Optional } from '@angular/core';
import { SERVER_URL } from 'app/core/variables';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DownloadService {
  private serverURL!: string;
  private downloadPipe: DownloadPipe;

  public constructor(
    private httpClient: HttpClient,
    @Optional()
    @Inject(SERVER_URL)
    serverURL: string
  ) {
    if (serverURL) {
      this.serverURL = serverURL;
    }

    this.downloadPipe = new DownloadPipe(this.serverURL);
  }

  public async getNodeContent(nodeId: string) {
    const url = this.downloadPipe.transform(nodeId);
    return await firstValueFrom(
      this.httpClient.get(url, { responseType: 'blob' })
    );
  }
}
