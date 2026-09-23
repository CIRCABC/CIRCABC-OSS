import { inject, Pipe, PipeTransform } from '@angular/core';
import { BASE_PATH } from 'app/core/generated/circabc';

/**
 * Pipe that builds the backend URL used to download several library nodes at
 * once as a single bulk archive.
 *
 * Given a list of node identifiers it produces a URL pointing at the
 * `/content/bulk` endpoint, appending each id as a `nodeIds` query parameter.
 * The endpoint host prefix is resolved from the injected {@link BASE_PATH}
 * token of the generated CIRCABC API client.
 *
 * Usage in a template: `{{ nodeIds | cbcBulkDownload }}`.
 */
@Pipe({
  name: 'cbcBulkDownload',
})
export class BulkDownloadPipe implements PipeTransform {
  /**
   * Base URL of the CIRCABC backend, resolved from the {@link BASE_PATH}
   * injection token and used as the prefix of the generated bulk-download URL.
   */
  private readonly basePath!: string;

  /**
   * Creates the pipe and resolves {@link basePath} from the injected
   * {@link BASE_PATH} token. The value is only assigned when the token
   * provides a non-empty base path.
   */
  public constructor() {
    const basePath = inject(BASE_PATH);

    if (basePath) {
      this.basePath = basePath;
    }
  }

  /**
   * Builds the bulk-download URL for the supplied node identifiers.
   *
   * Each defined identifier is appended as a `nodeIds` query parameter to the
   * `/content/bulk` endpoint. Undefined entries are skipped and the trailing
   * `&` separator is removed.
   *
   * @param nodeIds The node identifiers to include in the download; entries
   * may be `undefined` and are ignored.
   * @returns The fully qualified bulk-download URL.
   * @throws Error If `nodeIds` is not provided (falsy).
   */
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
    }
    throw new Error('The list of node ids should be provided');
  }
}
