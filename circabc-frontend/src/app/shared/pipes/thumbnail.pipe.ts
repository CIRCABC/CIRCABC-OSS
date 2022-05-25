import { Inject, Optional, Pipe, PipeTransform } from '@angular/core';
import { ALF_BASE_PATH } from 'app/core/variables';

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
      return `${this.alfBasePath}/node/workspace/${store}/${id}/content/thumbnails/doclib?c=queue&ph=true`;
    } else {
      throw new Error('id should be provided');
    }
  }
}
