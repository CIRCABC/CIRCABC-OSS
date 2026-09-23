import {
  ChangeDetectionStrategy,
  Component,
  CUSTOM_ELEMENTS_SCHEMA,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  MatProgressSpinnerModule,
  ProgressSpinnerMode,
} from '@angular/material/progress-spinner';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  IProvider,
  Msal2Provider,
  Providers,
  TemplateHelper,
} from '@microsoft/mgt';
import { Client } from '@microsoft/microsoft-graph-client';
import { DriveItem } from '@microsoft/microsoft-graph-types';
import { DownloadService } from 'app/core/download.service';
import { ContentService, Node, NodesService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { OfficeService } from 'app/core/office.service';
import { UploadService } from 'app/core/upload.service';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

/**
 * Standalone Angular component that bridges CIRCABC documents with Microsoft
 * Office through the Microsoft Graph API.
 *
 * The template renders a Microsoft Graph Toolkit login surface together with a
 * Material progress spinner that reflects the current background activity
 * (`spinnerMode`). Once the user has authenticated against Microsoft Entra ID
 * (MSAL2), the component orchestrates the "edit" and "update" flows for Office
 * documents:
 *
 * - In `edit` mode it checks out the CIRCABC node, uploads a working copy to
 *   the user's OneDrive and opens it in Office online for editing.
 * - In `update` mode it pulls the edited content back from OneDrive, updates
 *   the CIRCABC node content, checks the node back in and closes the window.
 *
 * The flow is driven by the `id` and `mode` query parameters of the current
 * route. Office integration is only activated when an Office client id is
 * configured in the environment.
 *
 * Key collaborators: {@link OfficeService} (OneDrive/Graph file operations),
 * {@link DownloadService} / {@link UploadService} (CIRCABC content transfer),
 * {@link ContentService} and {@link NodesService} (checkout/checkin and node
 * metadata), {@link LoginService} (current user) and {@link TranslocoService}
 * (translations).
 */
@Component({
  selector: 'cbc-office',
  templateUrl: './office.component.html',
  styleUrl: './office.component.scss',
  imports: [MatProgressSpinnerModule, TranslocoModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OfficeComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly downloadService = inject(DownloadService);
  private readonly officeService = inject(OfficeService);
  private readonly contentService = inject(ContentService);
  private readonly nodesService = inject(NodesService);
  private readonly uploadService = inject(UploadService);
  private readonly translateService = inject(TranslocoService);
  private readonly loginService = inject(LoginService);

  /** Identifier of the CIRCABC node being edited/updated, read from the `id` query parameter. */
  private id!: string;
  /** Operation to perform, read from the `mode` query parameter. Expected values: `'edit'` or `'update'`. */
  public readonly mode = signal<string>('');
  /** Display mode of the Material progress spinner: `'determinate'` when idle, `'indeterminate'` while working. */
  public readonly spinnerMode = signal<ProgressSpinnerMode>('determinate');
  /** Whether the user has successfully authenticated with Microsoft (drives the login UI state). */
  public isLogged = false;

  /**
   * Angular lifecycle hook. Loads the required translations, and—when Office
   * integration is enabled—configures the global MSAL2 provider and Graph
   * Toolkit binding syntax, then subscribes to the route query parameters to
   * capture the target node `id` and `mode`.
   *
   * If Office integration is disabled (no configured client id) the method
   * returns early without initialising the Microsoft provider.
   *
   * @returns A promise that resolves once translations are loaded and setup is complete.
   */
  ngOnInit(): void {
    void this.initialize();
  }

  /**
   * Loads translations and, when Office integration is enabled, initialises the
   * Microsoft provider and starts listening for the `id`/`mode` query
   * parameters. When Office integration is disabled (no configured client id)
   * it returns early without initialising the Microsoft provider.
   *
   * @returns A promise that resolves once translations are loaded and setup is
   * complete.
   */
  public async initialize(): Promise<void> {
    await this.setTranslations();

    this.spinnerMode.set('determinate');
    if (!this.isOfficeIntegrationEnabled()) {
      return;
    }

    Providers.globalProvider = new Msal2Provider({
      clientId: environment.officeClientId,
      scopes: ['Files.ReadWrite.All'],
    });
    TemplateHelper.setBindingSyntax('[[', ']]');

    this.route.queryParamMap.subscribe((queryParams) => {
      if (queryParams.has('id') && queryParams.has('mode')) {
        this.id = queryParams.get('id') as string;
        this.mode.set(queryParams.get('mode') as string);
      }
    });
  }

  /**
   * Sets the active translation language to the current user's locale and
   * eagerly preloads the Office-related translation labels used by the login
   * UI and the checkout/checkin flow.
   *
   * @returns A promise that resolves once all required labels have been loaded.
   */
  private async setTranslations() {
    this.translateService.setActiveLang(this.loginService.getCurrentUsername());
    await firstValueFrom(
      this.translateService.selectTranslate('label.office.login')
    );

    await firstValueFrom(
      this.translateService.selectTranslate('label.office.accounts')
    );
    await firstValueFrom(
      this.translateService.selectTranslate('label.office.edit')
    );
    await firstValueFrom(
      this.translateService.selectTranslate('label.office.update')
    );
  }

  /**
   * Handler invoked when the Microsoft login completes successfully. Marks the
   * user as logged in and kicks off the {@link init} orchestration flow.
   */
  public onLogin() {
    this.isLogged = true;
    this.init();
  }
  /**
   * Drives the main integration flow once a Microsoft provider is available.
   *
   * Ensures the CIRCABC root folder exists in OneDrive, resolves the target
   * node, then dispatches on `mode`:
   * - `'update'`: pushes the edited working copy back to CIRCABC, checks the
   *   node in, and closes the window.
   * - `'edit'`: uploads a working copy to OneDrive and opens it in Office.
   *
   * The spinner is switched to `'indeterminate'` while work is in progress and
   * restored to `'determinate'` when done.
   *
   * @returns A promise that resolves when the flow has completed.
   */
  private async init() {
    const provider: IProvider = Providers.globalProvider;

    if (provider) {
      this.spinnerMode.set('indeterminate');

      const graphClient = provider.graph.client;
      await this.createRootFolder(graphClient);

      const node = await this.nodesService.getNodeAsync({ id: this.id });

      if (this.mode() === 'update') {
        await this.updateWorkingCopy(node, graphClient);
        await this.checkIn(graphClient, node);
        window.close();
      } else if (this.mode() === 'edit') {
        const item = await this.uploadWorkingCopy(graphClient);
        window.open(item.webUrl, '_self');
      }
      this.spinnerMode.set('determinate');
    }
  }
  /**
   * Checks the given node back in to CIRCABC, marking it as a major version.
   * A localized check-in comment mentioning the current Microsoft user's
   * display name is generated and attached to the new version.
   *
   * @param graphClient The Microsoft Graph client used to resolve the current user's details.
   * @param node The CIRCABC node whose original node is being checked in.
   * @returns A promise that resolves once the check-in has been performed.
   */
  private async checkIn(graphClient: Client, node: Node) {
    const userDetails = await graphClient.api('me').get();
    const comment = await firstValueFrom(
      this.translateService.selectTranslate('label.office.check.in', {
        userDisplayName: userDetails.displayName,
      })
    );
    await this.contentService.putCheckinAsync({
      id: node?.properties?.originalNodeId as string,
      minorChange: true,
      keepCheckedOut: true,
      endEditInline: false,
      comment,
    });
  }
  /**
   * Ensures the CIRCABC working folder exists in the user's OneDrive, creating
   * it via {@link OfficeService.createFolder} if it is not already present.
   *
   * @param graphClient The Microsoft Graph client used to inspect and create the folder.
   * @returns A promise that resolves once the root folder is guaranteed to exist.
   */
  private async createRootFolder(graphClient: Client) {
    const circabcFolderExists = await this.officeService.rootFolderExists(
      graphClient,
      OfficeService.rootFolder
    );
    if (!circabcFolderExists) {
      const _circabcRoot: DriveItem = await this.officeService.createFolder(
        graphClient,
        OfficeService.rootFolder
      );
    }
  }

  /**
   * Pulls the edited content of an inline-editable node back from OneDrive and
   * updates the corresponding CIRCABC file content, then deletes the temporary
   * copy from OneDrive.
   *
   * Does nothing when the node is not flagged for inline editing
   * (`properties.editInline !== 'true'`).
   *
   * @param node The CIRCABC node being updated; provides the file name and target id.
   * @param graphClient The Microsoft Graph client used to fetch and delete the OneDrive file.
   * @returns A promise that resolves once the content has been updated and the copy removed.
   */
  private async updateWorkingCopy(node: Node, graphClient: Client) {
    if (node?.properties?.editInline === 'true') {
      const blob = await this.officeService.getFileContent(
        graphClient,
        OfficeService.rootFolder,
        node.name as string
      );
      const file = new File([blob], node.name as string);
      await this.uploadService.updateExistingFileContent(
        file,
        node.id as string,
        false
      );

      const _result = await this.officeService.deleteFile(
        graphClient,
        OfficeService.rootFolder,
        node.name as string
      );
    }
  }

  /**
   * Checks out the target CIRCABC node to create a working copy, downloads the
   * working copy content, and uploads it to the user's OneDrive using a large
   * file upload session so it can be opened in Office online.
   *
   * @param graphClient The Microsoft Graph client used to upload the working copy.
   * @returns A promise resolving to the uploaded OneDrive {@link DriveItem} (its `webUrl` is used to open Office).
   */
  private async uploadWorkingCopy(graphClient: Client) {
    const workingCopyId = await this.contentService.postCheckoutAsync({
      id: this.id,
      editInline: true,
    });
    const blob = await this.downloadService.getNodeContent(workingCopyId.id);
    const workingCopyNode = await this.nodesService.getNodeAsync({
      id: workingCopyId.id,
    });
    const item = await this.officeService.largeFileUpload(
      graphClient,
      blob,
      workingCopyNode.name as string
    );
    return item;
  }
  /**
   * Determines whether Microsoft Office integration is enabled for the current
   * environment, based on the presence of a configured Office client id.
   *
   * @returns `true` if an Office client id is configured, otherwise `false`.
   */
  private isOfficeIntegrationEnabled(): boolean {
    return environment.officeClientId !== '';
  }
}
