import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  Providers,
  Msal2Provider,
  TemplateHelper,
  IProvider,
} from '@microsoft/mgt';
import { DownloadService } from 'app/core/download.service';
import { DriveItem } from '@microsoft/microsoft-graph-types';
import { Client } from '@microsoft/microsoft-graph-client';

import { OfficeService } from 'app/core/office.service';
import { ContentService, Node, NodesService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';
import { UploadService } from 'app/core/upload.service';
import { environment } from 'environments/environment';
import { ProgressSpinnerMode } from '@angular/material/progress-spinner';
import { TranslocoService } from '@ngneat/transloco';
import { LoginService } from 'app/core/login.service';

@Component({
  selector: 'cbc-office',
  templateUrl: './office.component.html',
  styleUrls: ['./office.component.scss'],
})
export class OfficeComponent implements OnInit {
  private id!: string;
  public mode!: string;
  public spinnerMode: ProgressSpinnerMode = 'determinate';
  public isLogged = false;

  constructor(
    private route: ActivatedRoute,
    private downloadService: DownloadService,
    private officeService: OfficeService,
    private contentService: ContentService,
    private nodesService: NodesService,
    private uploadService: UploadService,
    private translateService: TranslocoService,
    private loginService: LoginService
  ) {}

  async ngOnInit(): Promise<void> {
    await this.setTranslations();

    this.spinnerMode = 'determinate';
    if (!this.isOfficeIntegrationEnabled()) {
      return;
    }

    Providers.globalProvider = new Msal2Provider({
      clientId: environment.officeClientId,
      scopes: ['Files.ReadWrite.All'],
    });
    TemplateHelper.setBindingSyntax('[[', ']]');

    this.route.queryParamMap.subscribe(async (queryParams) => {
      if (queryParams.has('id') && queryParams.has('mode')) {
        this.id = queryParams.get('id') as string;
        this.mode = queryParams.get('mode') as string;
      }
    });
  }

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

  public onLogin() {
    this.isLogged = true;
    this.init();
  }
  private async init() {
    const provider: IProvider = Providers.globalProvider;

    if (provider) {
      this.spinnerMode = 'indeterminate';

      const graphClient = provider.graph.client;
      await this.createRootFolder(graphClient);

      const node = await firstValueFrom(this.nodesService.getNode(this.id));

      if (this.mode === 'update') {
        await this.updateWorkingCopy(node, graphClient);
        await this.checkIn(graphClient, node);
        window.close();
      } else if (this.mode === 'edit') {
        const item = await this.uploadWorkingCopy(graphClient);
        window.open(item.webUrl, '_self');
      }
      this.spinnerMode = 'determinate';
    }
  }
  private async checkIn(graphClient: Client, node: Node) {
    const userDetails = await graphClient.api('me').get();
    const comment = await firstValueFrom(
      this.translateService.selectTranslate('label.office.check.in', {
        userDisplayName: userDetails.displayName,
      })
    );
    await firstValueFrom(
      this.contentService.putCheckin(
        node?.properties?.originalNodeId as string,
        comment,
        true,
        false,
        true
      )
    );
  }
  private async createRootFolder(graphClient: Client) {
    const circabcFolderExists = await this.officeService.rootFolderExists(
      graphClient,
      OfficeService.rootFolder
    );
    if (!circabcFolderExists) {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const circabcRoot: DriveItem = await this.officeService.createFolder(
        graphClient,
        OfficeService.rootFolder
      );
    }
  }

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
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const result = await this.officeService.deleteFile(
        graphClient,
        OfficeService.rootFolder,
        node.name as string
      );
    }
  }

  private async uploadWorkingCopy(graphClient: Client) {
    const workingCopyId = await firstValueFrom(
      this.contentService.postCheckout(this.id, true)
    );
    const blob = await this.downloadService.getNodeContent(workingCopyId.id);
    const workingCopyNode = await firstValueFrom(
      this.nodesService.getNode(workingCopyId.id)
    );
    const item = await this.officeService.largeFileUpload(
      graphClient,
      blob,
      workingCopyNode.name as string
    );
    return item;
  }
  private isOfficeIntegrationEnabled(): boolean {
    return environment.officeClientId !== '';
  }
}
