import { inject, Service } from '@angular/core';
import { SERVER_URL } from 'app/core/variables';

/**
 * Root-provided utility service responsible for building download URLs for
 * CIRCABC resources.
 *
 * It combines the application's configured backend base URL (injected via the
 * {@link SERVER_URL} token) with the REST download endpoint to produce the
 * absolute URL used to download a node by its identifier.
 *
 * Key collaborators:
 * - {@link SERVER_URL}: injection token providing the backend server base URL.
 */
@Service()
export class DownloadUtilService {
  /**
   * Base URL of the backend server, injected from the {@link SERVER_URL}
   * token. Used as the prefix for all generated download URLs.
   */
  private readonly serverURL = inject(SERVER_URL);

  /**
   * Builds the absolute REST download URL for the given resource identifier.
   *
   * @param id The identifier of the resource to download. Although declared as
   * optional, a value must be supplied; calling without an `id` results in an
   * error.
   * @returns The absolute download URL in the form
   * `{serverURL}rest/download/{id}`.
   * @throws {Error} If `id` is not provided (undefined or empty).
   */
  public getDownloadUrl(id?: string): string {
    const url = `${this.serverURL}rest/download/${id}`;
    if (id) {
      return url;
    }
    throw new Error('id should be provided');
  }
}
