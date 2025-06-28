import { Inject, Optional, Pipe, PipeTransform } from '@angular/core';
import { SERVER_URL } from 'app/core/variables';

@Pipe({
  name: 'cbcDownload',
  pure: true,
})
export class DownloadPipe implements PipeTransform {
  private serverURL!: string;
  public constructor(
    @Optional()
    @Inject(SERVER_URL)
    serverURL: string
  ) {
    if (serverURL) {
      this.serverURL = serverURL;
    }
  }

  public transform(id?: string): string {
    const url = `${this.serverURL}rest/download/${id}`;
    if (id) {
      return url;
    }
    throw new Error('id should be provided');
  }
}
