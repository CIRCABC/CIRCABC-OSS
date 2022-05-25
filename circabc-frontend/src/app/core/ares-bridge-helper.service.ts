import { Injectable } from '@angular/core';
import {
  AresBridgeService,
  Attachment,
  StoreDocumentRequest,
  StoreDocumentResponse,
} from 'app/core/generated/ares-bridge';

import { DownloadService } from 'app/core/download.service';
import {
  ExternalRepositoryService,
  ExternalRepoTransaction,
  Node as ModelNode,
  TicketRequestInfo,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { TranslocoService } from '@ngneat/transloco';
import { RedirectionService } from 'app/core/redirection.service';
import { firstValueFrom } from 'rxjs';
import { ExternalRepositoryData } from './generated/circabc';

@Injectable({
  providedIn: 'root',
})
export class AresBridgeHelperService {
  public constructor(
    private loginService: LoginService,
    private aresBridgeService: AresBridgeService,
    private downloadService: DownloadService,
    private externalRepositoryService: ExternalRepositoryService,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService,
    private redirectionService: RedirectionService
  ) {
    this.aresBridgeService.configuration.apiKeys = {};
  }

  public async isAresBridgeEnabled(groupId: string) {
    if (!environment.aresBridgeEnabled) {
      return false;
    }
    let result = false;

    const repos = await firstValueFrom(
      this.externalRepositoryService.getExternalRepositories(groupId)
    );
    if (repos.length > 0) {
      result = repos.findIndex((repo) => repo.name === 'AresBridge') > -1;
    }

    return result;
  }

  public async nodeLog(nodeId: string) {
    if (!environment.aresBridgeEnabled) {
      return [];
    }

    const logs = await firstValueFrom(
      this.externalRepositoryService.getExternalRepositoryNodeLog(
        nodeId,
        'AresBridge'
      )
    );

    return logs;
  }

  public async groupLog(groupId: string): Promise<ExternalRepositoryData[]> {
    if (!environment.aresBridgeEnabled) {
      return [];
    }

    const logs = await firstValueFrom(
      this.externalRepositoryService.getExternalRepositoryGroupLog(
        groupId,
        'AresBridge'
      )
    );

    return logs;
  }

  public async getAlreadySentToAresBridge(
    nodes: ModelNode[]
  ): Promise<ExternalRepositoryData[]> {
    const result: ExternalRepositoryData[] = [];

    for (const node of nodes) {
      const logs = await firstValueFrom(
        this.externalRepositoryService.getExternalRepositoryNodeLog(
          node.id as string,
          'AresBridge'
        )
      );
      if (logs.length === 0) {
        continue;
      } else {
        logs.forEach((log) => {
          if (
            log.nodeId === node.id &&
            (log.saveNumber || log.registrationNumber)
          ) {
            result.push(log);
          }
        });
      }
    }
    return result;
  }

  public async sendToAresBridge(nodeOrNodes: ModelNode | ModelNode[]) {
    // remember URL to return after Ares Bridge redirect
    this.redirectionService.mustRedirect();

    let nodeArray: ModelNode[];
    if (nodeOrNodes instanceof Array) {
      if (nodeOrNodes.length === 0) {
        return;
      } else {
        nodeArray = nodeOrNodes;
      }
    } else {
      nodeArray = [nodeOrNodes];
    }

    let documentDate: number | undefined;
    if (nodeArray[0].properties) {
      documentDate = new Date(nodeArray[0].properties.modified).getTime();
    }

    const username = this.loginService.getUser().userId as string;

    const hasUserAccessRequestDate = new Date().toUTCString();
    const hasUserAccessTicket = await this.getAresBridgeTicket(
      hasUserAccessRequestDate,
      '/user/access/' + username,
      'GET'
    );
    if (!hasUserAccessTicket) {
      return;
    }
    this.setTicket(hasUserAccessTicket);

    const hasUserAccess = await firstValueFrom(
      this.aresBridgeService.hasUserAccess(
        username,
        undefined,
        hasUserAccessRequestDate
      )
    );

    const attachments: Attachment[] = [];
    const attachment: Blob[] = [];
    let sequence = 1;
    for (const content of nodeArray) {
      const language = getLanguageEnumFromLocale(content.properties?.locale);
      attachments.push({
        sequence: sequence,
        filename: content.name as string,
        type: 'MAIN',
        language: language,
      });
      if (content.id) {
        const blob = await this.downloadService.getNodeContent(content.id);
        attachment.push(blob);
      }
      sequence = sequence + 1;
    }

    if (
      hasUserAccess.access.toString() === 'true' &&
      hasUserAccess.registrationRights.toString() === 'true'
    ) {
      const body: StoreDocumentRequest = {
        username: username,
        document: {
          title: nodeArray[0].name,
          levelOfSensitivity: 'NORMAL',
          documentDate: documentDate,
          attachments: attachments,
        },
        uiPreferences: {
          titleEditable: true,
          attachmentMetadataEditable: true,
        },
      };

      const storeDocumentRequestDate = new Date().toUTCString();
      const storeDocumentTicket = await this.getAresBridgeTicket(
        storeDocumentRequestDate,
        '/document',
        'POST'
      );
      if (!storeDocumentTicket) {
        return;
      }
      this.setTicket(storeDocumentTicket);

      const response = await firstValueFrom(
        this.aresBridgeService.storeDocument(
          body,
          attachment,
          undefined,
          storeDocumentRequestDate
        )
      );
      const externalRepoTransaction: ExternalRepoTransaction = {
        nodes: nodeArray,
        transactionId: response.transactionId,
      };
      await this.openAresBridge(externalRepoTransaction, response);
    } else {
      const message = this.translateService.translate(
        'error.ares.access.denied'
      );
      this.uiMessageService.addInfoMessage(message);
    }
  }

  private setTicket(ticket: string) {
    if (this.aresBridgeService.configuration.apiKeys) {
      this.aresBridgeService.configuration.apiKeys[
        'Authorization'
      ] = `AresBridge ${environment.aresBridgeKey}:${ticket}`;
    }
  }

  private async openAresBridge(
    externalRepoTransaction: ExternalRepoTransaction,
    response: StoreDocumentResponse
  ) {
    await firstValueFrom(
      this.externalRepositoryService.saveExternalRepoTransaction(
        'AresBridge',
        externalRepoTransaction
      )
    );

    const requestDate = new Date().toUTCString();
    const aresBridgeDate = encodeURIComponent(requestDate);
    const aresBridgeTicket = await this.getAresBridgeServerTicket(
      requestDate,
      '/Ares/bridge/ui',
      'GET'
    );

    // eslint-disable-next-line max-len
    const url = `${environment.aresBridgeUiURL}?token=${aresBridgeTicket}&apiKey=${environment.aresBridgeKey}&date=${aresBridgeDate}&transactionId=${response.transactionId}`;

    const win = window.open(url, '_blank');
    if (win) {
      win.focus();
    }
  }

  private async getAresBridgeServerTicket(
    requestDate: string,
    path: string,
    httpVerb: 'GET' | 'POST'
  ) {
    const fullPath = path;
    const ticketRequestInfo: TicketRequestInfo = {
      requestDate: requestDate,
      httpVerb: httpVerb,
      path: fullPath,
    };
    const ticket = await firstValueFrom(
      this.externalRepositoryService.getExternalRepoTicket(
        'AresBridge',
        ticketRequestInfo
      )
    );
    return ticket.ticket;
  }

  private async getAresBridgeTicket(
    requestDate: string,
    path: string,
    httpVerb: 'GET' | 'POST'
  ) {
    const fullPath =
      environment.aresBridgeURL.replace(environment.aresBridgeServer, '') +
      path;
    const ticketRequestInfo: TicketRequestInfo = {
      requestDate: requestDate,
      httpVerb: httpVerb,
      path: fullPath,
    };
    const ticket = await firstValueFrom(
      this.externalRepositoryService.getExternalRepoTicket(
        'AresBridge',
        ticketRequestInfo
      )
    );
    return ticket.ticket;
  }
}
function getLanguageEnumFromLocale(
  locale: string | undefined
): Attachment.LanguageEnum {
  if (locale === undefined) {
    return 'NS';
  }
  const language = locale.toUpperCase().substring(0, 2);
  if (Object.keys(Attachment.LanguageEnum).includes(language)) {
    return Attachment.LanguageEnum[
      language as keyof typeof Attachment.LanguageEnum
    ];
  } else {
    return 'NS';
  }
}
