import { inject, Pipe, PipeTransform } from '@angular/core';
import { DownloadUtilService } from 'app/shared/services/download-util.service';

/**
 * Pure Angular pipe that converts a node identifier into a fully-resolved
 * download URL.
 *
 * Used in templates (via the `cbcDownload` name) to turn a document/node id
 * into an href suitable for downloading the corresponding resource. The actual
 * URL construction is delegated to {@link DownloadUtilService}, keeping the
 * pipe a thin, reusable presentation-layer wrapper.
 *
 * Being marked `pure: true`, it is only re-evaluated when its input reference
 * changes, which is appropriate since the same id always maps to the same URL.
 *
 * @example
 * ```html
 * <a [href]="node.id | cbcDownload">Download</a>
 * ```
 */
@Pipe({
  name: 'cbcDownload',
  pure: true,
})
export class DownloadPipe implements PipeTransform {
  /**
   * Service collaborator responsible for building download URLs from node ids.
   */
  private readonly downloadUtil = inject(DownloadUtilService);

  /**
   * Transforms a node identifier into its download URL.
   *
   * @param id - The identifier of the node/document to download. May be
   * `undefined`, in which case the underlying service decides the resulting
   * (typically empty or fallback) URL.
   * @returns The download URL string produced by {@link DownloadUtilService.getDownloadUrl}.
   */
  public transform(id?: string): string {
    return this.downloadUtil.getDownloadUrl(id);
  }
}
