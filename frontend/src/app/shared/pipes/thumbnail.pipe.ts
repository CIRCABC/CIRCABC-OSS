import { inject, Pipe, PipeTransform } from '@angular/core';
import { ALF_BASE_PATH } from 'app/core/variables';
import { environment } from 'environments/environment';

/**
 * Pure Angular pipe (`cbcThumbnail`) that builds the URL used to fetch the
 * document-library thumbnail rendition ("doclib") for a given node.
 *
 * The generated URL depends on the active backend mode:
 * - When {@link environment.useAlfrescoAPI} is enabled, it returns a URL
 *   pointing to the public Alfresco REST renditions endpoint.
 * - Otherwise it returns a legacy CIRCABC content URL built from the injected
 *   Alfresco base path ({@link ALF_BASE_PATH}), selecting the appropriate
 *   store depending on whether the node is a version.
 *
 * Being pure, it is only re-evaluated when its input arguments change.
 */
@Pipe({
  name: 'cbcThumbnail',
  pure: true,
})
export class ThumbnailPipe implements PipeTransform {
  /** Base path of the Alfresco backend, injected from {@link ALF_BASE_PATH}. */
  private readonly alfBasePath!: string;

  /**
   * Resolves the Alfresco base path via dependency injection and stores it for
   * later URL construction. The path is only assigned when a non-empty value
   * is provided by the injector.
   */
  public constructor() {
    const alfBasePath = inject(ALF_BASE_PATH);

    if (alfBasePath) {
      this.alfBasePath = alfBasePath;
    }
  }

  /**
   * Builds the thumbnail (doclib rendition) content URL for the given node.
   *
   * @param id - The identifier of the node whose thumbnail URL is requested.
   * @param isVersion - When `true`, the node is treated as a version and the
   * `version2Store` is used instead of the default `SpacesStore` (only relevant
   * for the legacy, non-Alfresco-API URL form).
   * @returns The fully-qualified URL pointing to the node's doclib thumbnail
   * content.
   * @throws Error If no `id` is provided.
   */
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
