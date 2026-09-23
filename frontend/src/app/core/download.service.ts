import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { DownloadUtilService } from 'app/shared/services/download-util.service';
import { firstValueFrom } from 'rxjs';

/**
 * Application-wide service responsible for retrieving the raw binary content
 * of a repository node (e.g. a document stored in the CIRCABC library).
 *
 * It resolves the appropriate download URL for a node and fetches its content
 * as a {@link Blob}, which callers can then save, preview or otherwise process.
 *
 * Key collaborators:
 * - {@link HttpClient}: performs the actual HTTP request to fetch the content.
 * - {@link DownloadUtilService}: builds the backend download URL for a node.
 *
 * Registered as a singleton via `providedIn: 'root'`.
 */
@Service()
export class DownloadService {
  /** Angular HTTP client used to request the node content as a binary blob. */
  private readonly httpClient = inject(HttpClient);
  /** Helper service that resolves the backend download URL for a given node. */
  private readonly downloadUtil = inject(DownloadUtilService);

  /**
   * Fetches the binary content of the given repository node.
   *
   * The download URL is resolved through {@link DownloadUtilService} and the
   * response is requested with a `blob` response type so that arbitrary file
   * types are handled correctly.
   *
   * @param nodeId - Identifier of the repository node whose content is fetched.
   * @returns A promise resolving to the node content as a {@link Blob}.
   * @throws {@link HttpErrorResponse} If the underlying HTTP request fails.
   */
  public async getNodeContent(nodeId: string) {
    const url = this.downloadUtil.getDownloadUrl(nodeId);
    return await firstValueFrom(
      this.httpClient.get(url, { responseType: 'blob' })
    );
  }
}
