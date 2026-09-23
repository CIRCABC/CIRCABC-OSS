import { inject, Pipe, PipeTransform } from '@angular/core';
import { SERVER_URL } from 'app/core/variables';

/**
 * Pure Angular pipe (`cbcOldDownload`) that builds a legacy direct-download URL
 * for a node stored in the Alfresco `SpacesStore` workspace.
 *
 * Given a node identifier and file name, it produces an absolute URL of the form
 * `<serverURL>d/a/workspace/SpacesStore/<id>/<name>` that points at the backend's
 * legacy download endpoint. The pipe is `pure`, so the URL is only recomputed when
 * its input arguments change.
 *
 * @example
 * ```html
 * <a [href]="node.id | cbcOldDownload: node.name">Download</a>
 * ```
 */
@Pipe({
  name: 'cbcOldDownload',
  pure: true,
})
export class OldDownloadPipe implements PipeTransform {
  /**
   * Base server URL, injected from the {@link SERVER_URL} token, used as the prefix
   * of every generated download URL. Assigned once during construction.
   */
  private readonly serverURL!: string;

  /**
   * Creates the pipe and captures the application's server URL from the
   * {@link SERVER_URL} injection token. When the token resolves to a truthy value
   * it is stored in {@link serverURL} for later URL construction.
   */
  public constructor() {
    const serverURL = inject(SERVER_URL);

    if (serverURL) {
      this.serverURL = serverURL;
    }
  }

  /**
   * Builds the legacy download URL for the given node.
   *
   * @param id - The identifier of the node within the Alfresco `SpacesStore`.
   * @param name - The file name of the node, appended to the URL path.
   * @returns The absolute download URL
   *   `<serverURL>d/a/workspace/SpacesStore/<id>/<name>`.
   * @throws {Error} If either `id` or `name` is missing (undefined/empty).
   */
  public transform(id: string | undefined, name: string | undefined): string {
    if (id && name) {
      return `${this.serverURL}d/a/workspace/SpacesStore/${id}/${name}`;
    }
    throw new Error('id and name should be provided');
  }
}
