import { inject, Service } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { DownloadService } from 'app/core/download.service';
import {
  AresBridgeService,
  Attachment,
  StoreDocumentRequest,
  StoreDocumentResponse,
} from 'app/core/generated/ares-bridge';
import {
  ExternalRepositoryService,
  ExternalRepoTransaction,
  Node as ModelNode,
  TicketRequestInfo,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { RedirectionService } from 'app/core/redirection.service';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';
import { ExternalRepositoryData } from './generated/circabc';

/**
 * Root-scoped service that coordinates the integration between CIRCABC and the
 * external ARES Bridge document-registration system.
 *
 * Its responsibilities include:
 * - Checking whether the ARES Bridge feature is enabled for a given group.
 * - Retrieving ARES Bridge activity logs at node and group level.
 * - Determining which nodes have already been sent to ARES Bridge.
 * - Building the store-document request, downloading node contents as
 *   attachments, verifying the current user's registration rights, submitting
 *   the document to ARES Bridge and opening the ARES Bridge UI.
 *
 * Key collaborators:
 * - {@link LoginService} — provides the currently authenticated user.
 * - {@link AresBridgeService} — generated client used to talk to the ARES
 *   Bridge API (access checks and document storage).
 * - {@link DownloadService} — fetches node binary content to attach.
 * - {@link ExternalRepositoryService} — generated CIRCABC client used for
 *   repository discovery, logs, transactions and ticket generation.
 * - {@link UiMessageService} — surfaces user-facing messages (e.g. access
 *   denied).
 * - {@link TranslocoService} — translates message keys.
 * - {@link RedirectionService} — remembers the current URL so the user can be
 *   returned after the ARES Bridge redirect.
 */
@Service()
export class AresBridgeHelperService {
  private readonly loginService = inject(LoginService);
  private readonly aresBridgeService = inject(AresBridgeService);
  private readonly downloadService = inject(DownloadService);
  private readonly externalRepositoryService = inject(
    ExternalRepositoryService
  );
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translateService = inject(TranslocoService);
  private readonly redirectionService = inject(RedirectionService);

  /**
   * Creates the service and resets the ARES Bridge client's API key map to an
   * empty object so authorization tickets can be injected later per request via
   * {@link AresBridgeHelperService.setTicket}.
   */
  public constructor() {
    this.aresBridgeService.configuration.apiKeys = {};
  }

  /**
   * Determines whether the ARES Bridge integration is available for a group.
   *
   * Returns `false` immediately when the feature is disabled by environment
   * configuration; otherwise it queries the group's external repositories and
   * checks whether one named `AresBridge` is configured.
   *
   * @param groupId - Identifier of the interest group to inspect.
   * @returns A promise resolving to `true` if ARES Bridge is enabled and
   * configured for the group, otherwise `false`.
   */
  public async isAresBridgeEnabled(groupId: string) {
    if (!environment.aresBridgeEnabled) {
      return false;
    }
    let result = false;

    const repos =
      await this.externalRepositoryService.getExternalRepositoriesAsync({
        id: groupId,
      });
    if (repos.length > 0) {
      result = repos.some((repo) => repo.name === 'AresBridge');
    }

    return result;
  }

  /**
   * Retrieves the ARES Bridge activity log for a single node.
   *
   * @param nodeId - Identifier of the node whose ARES Bridge log is requested.
   * @returns A promise resolving to the list of log entries, or an empty array
   * when the ARES Bridge feature is disabled.
   */
  public async nodeLog(nodeId: string) {
    if (!environment.aresBridgeEnabled) {
      return [];
    }

    const logs =
      await this.externalRepositoryService.getExternalRepositoryNodeLogAsync({
        id: nodeId,
        repoId: 'AresBridge',
      });

    return logs;
  }

  /**
   * Retrieves the ARES Bridge activity log for an entire group.
   *
   * @param groupId - Identifier of the group whose ARES Bridge log is requested.
   * @returns A promise resolving to the list of {@link ExternalRepositoryData}
   * log entries, or an empty array when the ARES Bridge feature is disabled.
   */
  public async groupLog(groupId: string): Promise<ExternalRepositoryData[]> {
    if (!environment.aresBridgeEnabled) {
      return [];
    }

    const logs =
      await this.externalRepositoryService.getExternalRepositoryGroupLogAsync({
        id: groupId,
        repoId: 'AresBridge',
      });

    return logs;
  }

  /**
   * Inspects the given nodes and collects the ARES Bridge log entries for those
   * that have already been sent (i.e. entries that carry a save number or a
   * registration number for the matching node).
   *
   * @param nodes - The nodes to check against their ARES Bridge logs.
   * @returns A promise resolving to the list of {@link ExternalRepositoryData}
   * entries indicating nodes already submitted to ARES Bridge.
   */
  public async getAlreadySentToAresBridge(
    nodes: ModelNode[]
  ): Promise<ExternalRepositoryData[]> {
    const result: ExternalRepositoryData[] = [];

    for (const node of nodes) {
      const logs =
        await this.externalRepositoryService.getExternalRepositoryNodeLogAsync({
          id: node.id as string,
          repoId: 'AresBridge',
        });
      if (logs.length !== 0) {
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

  /**
   * Submits one or more nodes to ARES Bridge for registration.
   *
   * The flow:
   * 1. Records the current URL so the user can be returned after the ARES
   *    Bridge redirect.
   * 2. Normalizes the input into an array (aborting if an empty array is
   *    given).
   * 3. Verifies the current user's access and registration rights via the ARES
   *    Bridge API (aborting, or showing an access-denied message, when
   *    insufficient).
   * 4. Downloads each node's content and builds the corresponding attachments.
   * 5. Sends the store-document request and opens the ARES Bridge UI for the
   *    resulting transaction.
   *
   * @param nodeOrNodes - A single node or an array of nodes to register. The
   * first node supplies the document title and modified date used as the
   * document date.
   * @returns A promise that resolves when the submission flow completes. It
   * resolves early (without submitting) when the input array is empty, when a
   * required authorization ticket cannot be obtained, or when the user lacks
   * access/registration rights.
   */
  public async sendToAresBridge(nodeOrNodes: ModelNode | ModelNode[]) {
    // remember URL to return after Ares Bridge redirect
    this.redirectionService.mustRedirect();

    let nodeArray: ModelNode[];
    if (Array.isArray(nodeOrNodes)) {
      if (nodeOrNodes.length === 0) {
        return;
      }
      nodeArray = nodeOrNodes;
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
      `/user/access/${username}`,
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

  /**
   * Injects an ARES Bridge authorization ticket into the generated client's
   * configuration so it is sent as the `Authorization` header on the next
   * request. The header combines the configured ARES Bridge key with the
   * per-request ticket.
   *
   * @param ticket - The signed ticket returned by the ticket endpoint.
   */
  private setTicket(ticket: string) {
    if (this.aresBridgeService.configuration.apiKeys) {
      this.aresBridgeService.configuration.apiKeys['Authorization'] =
        `AresBridge ${environment.aresBridgeKey}:${ticket}`;
    }
  }

  /**
   * Persists the external-repository transaction on the CIRCABC side and then
   * opens the ARES Bridge UI in a new browser tab, focusing it if available.
   *
   * The UI URL is built from the environment configuration together with a
   * freshly generated server ticket, the API key, the request date and the
   * transaction identifier.
   *
   * @param externalRepoTransaction - The transaction (nodes plus transaction
   * id) to persist against the `AresBridge` repository.
   * @param response - The store-document response providing the transaction id
   * used to open the ARES Bridge UI.
   * @returns A promise that resolves once the transaction is saved and the UI
   * window has been opened.
   */
  private async openAresBridge(
    externalRepoTransaction: ExternalRepoTransaction,
    response: StoreDocumentResponse
  ) {
    await this.externalRepositoryService.saveExternalRepoTransactionAsync({
      id: 'AresBridge',
      externalRepoTransaction,
    });

    const requestDate = new Date().toUTCString();
    const aresBridgeDate = encodeURIComponent(requestDate);
    const aresBridgeTicket = await this.getAresBridgeServerTicket(
      requestDate,
      '/Ares/bridge/ui',
      'GET'
    );

    const url = `${environment.aresBridgeUiURL}?token=${aresBridgeTicket}&apiKey=${environment.aresBridgeKey}&date=${aresBridgeDate}&transactionId=${response.transactionId}`;

    const win = window.open(url, '_blank');
    if (win) {
      win.focus();
    }
  }

  /**
   * Requests a signed ticket for a call made directly against the ARES Bridge
   * *server* UI path (the path is used verbatim, without rewriting).
   *
   * @param requestDate - The request date (UTC string) to sign.
   * @param path - The server-relative path the ticket authorizes.
   * @param httpVerb - The HTTP method the ticket authorizes (`GET` or `POST`).
   * @returns A promise resolving to the signed ticket string.
   */
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
    const ticket =
      await this.externalRepositoryService.getExternalRepoTicketAsync({
        id: 'AresBridge',
        ticketRequestInfo,
      });
    return ticket.ticket;
  }

  /**
   * Requests a signed ticket for a call made against the ARES Bridge *API*.
   *
   * The full path is computed by stripping the configured ARES Bridge server
   * prefix from the ARES Bridge URL and appending the given path, so the ticket
   * is signed for the correct API-relative path.
   *
   * @param requestDate - The request date (UTC string) to sign.
   * @param path - The API path (relative to the ARES Bridge API root) the
   * ticket authorizes.
   * @param httpVerb - The HTTP method the ticket authorizes (`GET` or `POST`).
   * @returns A promise resolving to the signed ticket string.
   */
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
    const ticket =
      await this.externalRepositoryService.getExternalRepoTicketAsync({
        id: 'AresBridge',
        ticketRequestInfo,
      });
    return ticket.ticket;
  }
}
/**
 * Maps a CIRCABC locale string to the corresponding ARES Bridge attachment
 * language enum value.
 *
 * The first two characters of the locale are upper-cased and matched against
 * the {@link Attachment.LanguageEnum} keys.
 *
 * @param locale - The node locale (e.g. `en`, `fr_FR`), or `undefined`.
 * @returns The matching {@link Attachment.LanguageEnum} value, or `'NS'` (not
 * specified) when the locale is `undefined` or has no matching language.
 */
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
  }
  return 'NS';
}
