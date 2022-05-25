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

  public transform(id: string | undefined): string {
    if (id) {
      return `${this.serverURL}rest/download/${id}`;
    } else {
      throw new Error('id should be provided');
    }
  }
}
