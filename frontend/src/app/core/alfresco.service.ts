import type { AlfrescoApi, RenditionEntry } from '@alfresco/js-api';
import { inject, Service } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

/**
 * Identifiers of the rendition (alternate representation) types that can be
 * requested from the Alfresco backend for a given node.
 *
 * - `avatar` / `avatar32`: user avatar renditions.
 * - `doclib`: document library thumbnail.
 * - `imgpreview`: image preview.
 * - `medium`: medium-sized image rendition.
 * - `pdf`: PDF rendition of the document.
 */
type RenditionId =
  'avatar' | 'avatar32' | 'doclib' | 'imgpreview' | 'medium' | 'pdf';

/**
 * Root-provided service that mediates access to the Alfresco ECM backend via
 * the `@alfresco/js-api` client, focusing on node renditions.
 *
 * The service lazily instantiates and caches a single {@link AlfrescoApi}
 * client (configured with the ECM host from the environment and the current
 * authentication ticket) and dynamically imports the heavy `@alfresco/js-api`
 * module only when a rendition operation is actually performed.
 *
 * Key collaborators:
 * - {@link LoginService}: supplies the ECM authentication ticket.
 * - `environment.alfrescoHost`: the ECM host the client connects to.
 */
@Service()
export class AlfrescoService {
  /** Provides the current ECM authentication ticket used to configure the API client. */
  private readonly loginService = inject(LoginService);

  /**
   * Cached promise resolving to the lazily created {@link AlfrescoApi} client.
   * `null` until the first call that requires the client; reused thereafter to
   * avoid re-instantiating the client on every request.
   */
  private alfrescoApiPromise: Promise<AlfrescoApi> | null = null;

  /**
   * Lazily creates (once) and returns the shared {@link AlfrescoApi} client.
   *
   * On first invocation it dynamically imports `@alfresco/js-api` and builds an
   * `AlfrescoApi` instance using the configured ECM host and the current login
   * ticket, caching the resulting promise for subsequent calls.
   *
   * @returns A promise that resolves to the shared {@link AlfrescoApi} client.
   */
  private async getAlfrescoApi(): Promise<AlfrescoApi> {
    this.alfrescoApiPromise ??= import('@alfresco/js-api').then((module) => {
      return new module.AlfrescoApi({
        hostEcm: environment.alfrescoHost,
        ticketEcm: this.loginService.getTicket(),
      });
    });
    return this.alfrescoApiPromise;
  }

  /**
   * Requests the Alfresco backend to (asynchronously) generate a rendition of
   * the given type for the specified node.
   *
   * @param nodeId - Identifier of the node for which the rendition is created.
   * @param renditionId - The type of rendition to generate.
   * @returns A promise for the underlying `RenditionsApi.createRendition` call,
   * which triggers server-side generation of the rendition.
   */
  public async createRendition(nodeId: string, renditionId: RenditionId) {
    const alfrescoApi = await this.getAlfrescoApi();
    const { RenditionsApi } = await import('@alfresco/js-api');
    const renditionsApi = new RenditionsApi(alfrescoApi);
    const renditionBodyCreate = { id: `${renditionId}` };

    return renditionsApi.createRendition(nodeId, renditionBodyCreate);
  }

  /**
   * Retrieves the metadata for an existing rendition of the given type for the
   * specified node.
   *
   * @param nodeId - Identifier of the node whose rendition is retrieved.
   * @param renditionId - The type of rendition to retrieve.
   * @returns A promise resolving to the {@link RenditionEntry} describing the
   * requested rendition.
   */
  public async getRendition(
    nodeId: string,
    renditionId: RenditionId
  ): Promise<RenditionEntry> {
    const alfrescoApi = await this.getAlfrescoApi();
    const { RenditionsApi } = await import('@alfresco/js-api');
    const renditionsApi = new RenditionsApi(alfrescoApi);

    return renditionsApi.getRendition(nodeId, renditionId);
  }
}
