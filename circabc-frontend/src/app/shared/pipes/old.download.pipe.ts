import { Inject, Optional, Pipe, PipeTransform } from '@angular/core';
import { SERVER_URL } from 'app/core/variables';

@Pipe({
  name: 'cbcOldDownload',
  pure: true,
})
export class OldDownloadPipe implements PipeTransform {
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

  public transform(id: string | undefined, name: string | undefined): string {
    if (id && name) {
      return `${this.serverURL}d/a/workspace/SpacesStore/${id}/${name}`;
    } else {
      throw new Error('id and name should be provided');
    }
  }
}
