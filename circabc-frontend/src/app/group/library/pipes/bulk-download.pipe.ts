import {
  Inject,
  Injectable,
  Optional,
  Pipe,
  PipeTransform,
} from '@angular/core';
import { BASE_PATH } from 'app/core/generated/circabc';

@Pipe({
  name: 'cbcBulkDownload',
})
@Injectable()
export class BulkDownloadPipe implements PipeTransform {
  private basePath!: string;

  public constructor(
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

  public transform(nodeIds: (string | undefined)[]): string {
    if (nodeIds) {
      let endURL = '';
      for (const nodeId of nodeIds) {
        if (nodeId) {
          endURL += `nodeIds=${nodeId}&`;
        }
      }
      // remove last &
      if (endURL.length > 0) {
        endURL = endURL.substring(0, endURL.length - 1);
      }
      return `${this.basePath}/content/bulk?${endURL}`;
    } else {
      throw new Error('The list of node ids should be provided');
    }
  }
}
