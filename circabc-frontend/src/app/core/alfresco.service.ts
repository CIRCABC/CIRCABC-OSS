import { Injectable } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import type { AlfrescoApi, RenditionEntry } from '@alfresco/js-api';

type RenditionId =
  | 'avatar'
  | 'avatar32'
  | 'doclib'
  | 'imgpreview'
  | 'medium'
  | 'pdf';

@Injectable({
  providedIn: 'root',
})
export class AlfrescoService {
  private alfrescoApiPromise: Promise<AlfrescoApi> | null = null;

  constructor(private loginService: LoginService) {}

  private async getAlfrescoApi(): Promise<AlfrescoApi> {
    if (!this.alfrescoApiPromise) {
      this.alfrescoApiPromise = import('@alfresco/js-api').then((module) => {
        return new module.AlfrescoApi({
          hostEcm: environment.alfrescoHost,
          ticketEcm: this.loginService.getTicket(),
        });
      });
    }
    return this.alfrescoApiPromise;
  }

  public async createRendition(nodeId: string, renditionId: RenditionId) {
    const alfrescoApi = await this.getAlfrescoApi();
    const { RenditionsApi } = await import('@alfresco/js-api');
    const renditionsApi = new RenditionsApi(alfrescoApi);
    const renditionBodyCreate = { id: `${renditionId}` };

    return renditionsApi.createRendition(nodeId, renditionBodyCreate);
  }

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
