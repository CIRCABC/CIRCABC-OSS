import { Inject, Optional, Pipe, PipeTransform } from '@angular/core';
import { ALF_BASE_PATH } from 'app/core/variables';
import { environment } from 'environments/environment';

@Pipe({
  name: 'cbcThumbnail',
  pure: true,
})
export class ThumbnailPipe implements PipeTransform {
  private alfBasePath!: string;
  public constructor(
    @Optional()
    @Inject(ALF_BASE_PATH)
    alfBasePath: string
  ) {
    if (alfBasePath) {
      this.alfBasePath = alfBasePath;
    }
  }

  public transform(id: string, isVersion?: boolean): string {
    if (id) {
      let store = 'SpacesStore';
      if (isVersion) {
        store = 'version2Store';
      }
      if (environment.useAlfrescoAPI) {
        return `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/${id}/renditions/doclib/content`;
      }
      return `${this.alfBasePath}/node/workspace/${store}/${id}/content/thumbnails/doclib?c=queue&ph=true`;
    }
    throw new Error('id should be provided');
  }
}
