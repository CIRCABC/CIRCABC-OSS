import { NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  FileService,
  Node as ModelNode,
  NodesService,
  PermissionDefinition,
  PermissionService,
  TranslationsService,
} from 'app/core/generated/circabc';
import { FileInputComponent } from 'app/group/library/upload-form/file-input/file-input.component';
import { FileListComponent } from 'app/group/library/upload-form/file-list/file-list.component';
import { FileMetadataComponent } from 'app/group/library/upload-form/file-metadata/file-metadata.component';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-upload-form',
  templateUrl: './upload-form.component.html',
  styleUrl: './upload-form.component.scss',
  imports: [
    FileInputComponent,
    NgClass,
    FileListComponent,
    FileMetadataComponent,
    MatSlideToggleModule,
    ReactiveFormsModule,
    DataCyDirective,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class UploadFormComponent implements OnInit {
  public filesToUpload: FileUploadItem[] = [];
  public pivotDocuments: FileUploadItem[] = [];
  public translations: FileUploadItem[] = [];
  public fileSelected: FileUploadItem | undefined;
  public targetNodeId!: string;
  public targetNode: ModelNode | undefined;
  public uploadingFileName: string | undefined;
  public uploadingProgress = 0;
  public uploading = false;
  public uploadFinished = false;
  public canceled = false;
  public maxFileSize = 1024 * 1024 * 300; // 300MB

  public notify = new FormControl(true);

  public perms!: PermissionDefinition;

  constructor(
    private permissionService: PermissionService,
    private fileService: FileService,
    private route: ActivatedRoute,
    private router: Router,
    private translationsService: TranslationsService,
    private nodesService: NodesService,
    private dialog: MatDialog,
    private translateService: TranslocoService
  ) {}

  async ngOnInit() {
    this.route.params.subscribe((params) => {
      if (params?.nodeId) {
        this.targetNodeId = params.nodeId;
      }
    });
    this.targetNode = await firstValueFrom(
      this.nodesService.getNode(this.targetNodeId)
    );
  }

  public addNewFiles(files: FileUploadItem[]) {
    for (const file of files) {
      if (!this.isFileInSelection(file)) {
        this.filesToUpload.push(file);
        this.fileSelected = file;
      }
    }
    if (environment.circabcRelease === 'echa') {
      this.showDialogSimpleMsg();
    }
  }

  public isFileInSelection(file: FileUploadItem) {
    return (
      this.filesToUpload.find((fileUploadItem) => {
        return file.name === fileUploadItem.name;
      }) !== undefined
    );
  }

  public fileListChanged(files: FileUploadItem[]) {
    this.fileSelected = undefined;
    this.filesToUpload = files;
    this.filesToUpload.forEach((file) => {
      if (file.selected) {
        this.fileSelected = file;
      }
    });
  }

  public propagateFileChange(file: FileUploadItem) {
    const idx = this.filesToUpload.findIndex((fileItem) => {
      return fileItem.id === file.id;
    });

    const wasPivot = this.filesToUpload[idx].isPivot && !file.isPivot;

    if (idx) {
      this.filesToUpload[idx] = file;
    }

    this.preparePivotDocuments();

    if (wasPivot) {
      this.removePivotTranslations(file);
    } else {
      this.prepareTranslations();
    }
  }

  private async uploadFilesOrPivot() {
    let nbPivot = 0;
    let currentPivot = 0;
    this.filesToUpload.forEach((fileUpload) => {
      if (fileUpload.isPivot) {
        nbPivot += 1;
      }
    });
    // start with all normal files and pivot files
    for (const fileUpload of this.filesToUpload) {
      if (
        fileUpload.file.size <= this.maxFileSize &&
        !fileUpload.isTranslation
      ) {
        fileUpload.uploadStatus = 'uploading';
        const expirationDate = JSON.stringify(fileUpload.expirationDate);
        try {
          const end = await firstValueFrom(
            this.fileService.uploadFile(
              this.targetNodeId,
              fileUpload.name,
              fileUpload.title,
              fileUpload.description,
              fileUpload.keywords,
              fileUpload.author,
              fileUpload.reference,
              expirationDate ? expirationDate.replace('"', '') : undefined,
              fileUpload.securityRanking,
              fileUpload.status,
              fileUpload.isPivot,
              fileUpload.lang,
              this.getDynProp(fileUpload),
              fileUpload.file
            )
          );
          fileUpload.uploadStatus = 'finished';
          fileUpload.nodeRef = end.nodeRef;

          this.verifyEndUpload();
          if (fileUpload.isPivot && currentPivot < nbPivot) {
            currentPivot += 1;
          }
          if (currentPivot === nbPivot) {
            this.uploadTranslations();
          }
        } catch (error) {
          this.onError(error, fileUpload);
        }

        if (
          (fileUpload.securityRanking === 'SENSITIVE' ||
            fileUpload.securityRanking === 'SPECIAL_HANDLING') &&
          environment.circabcRelease === 'echa'
        ) {
          if (fileUpload.nodeRef) {
            await this.cutInheritance(fileUpload.nodeRef);
          }
        }
      } else if (
        fileUpload.file.size > this.maxFileSize &&
        !fileUpload.isTranslation
      ) {
        fileUpload.uploadStatus = 'error';
        this.verifyEndUpload();
      }
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private onError(error: unknown, fileUpload: FileUploadItem) {
    console.error(error);
    fileUpload.uploadStatus = 'error';
    this.uploadingProgress = this.uploadingProgress + 1;
  }

  private verifyEndUpload() {
    this.uploadingProgress = this.uploadingProgress + 1;
    if (this.uploadingProgress === this.filesToUpload.length) {
      this.uploadFinished = true;
      this.uploading = false;
      if (this.notify.value) {
        this.fireNotifications();
      }
    }
  }

  private async uploadTranslations() {
    // proceed with translations
    for (const fileUpload of this.filesToUpload) {
      if (
        fileUpload.file.size <= this.maxFileSize &&
        fileUpload.isTranslation &&
        fileUpload.lang &&
        fileUpload.translationOf
      ) {
        fileUpload.uploadStatus = 'uploading';
        const nodeRef = this.getNodeRefOfPivot(fileUpload.translationOf);
        const expirationDate = JSON.stringify(fileUpload.expirationDate);
        try {
          await firstValueFrom(
            this.translationsService.postTranslationEnhanced(
              nodeRef,
              false,
              fileUpload.name,
              fileUpload.title,
              fileUpload.description,
              fileUpload.keywords,
              fileUpload.author,
              fileUpload.reference,
              expirationDate ? expirationDate.replace('"', '') : undefined,
              fileUpload.securityRanking,
              fileUpload.status,
              fileUpload.lang,
              this.getDynProp(fileUpload),
              fileUpload.file
            )
          );

          fileUpload.uploadStatus = 'finished';
          this.verifyEndUpload();
        } catch (error) {
          this.onError(error, fileUpload);
        }
      } else if (
        fileUpload.file.size > this.maxFileSize &&
        fileUpload.isTranslation
      ) {
        fileUpload.uploadStatus = 'error';
        this.verifyEndUpload();
      }
    }
  }

  public uploadFiles() {
    this.uploading = true;
    this.uploadFilesOrPivot();
  }

  public startNewUpload() {
    this.uploadingProgress = 0;
    this.uploadFinished = false;
    this.filesToUpload = [];
    this.fileSelected = undefined;
  }

  public preparePivotDocuments() {
    this.pivotDocuments = this.filesToUpload.filter((file) => {
      return file.isPivot;
    });
  }

  public removePivotTranslations(file: FileUploadItem) {
    this.filesToUpload.forEach((fileUpload) => {
      if (fileUpload.isTranslation && fileUpload.translationOf === file.id) {
        fileUpload.isTranslation = false;
        fileUpload.lang = '';
      }
    });

    this.prepareTranslations();
  }

  public prepareTranslations() {
    this.translations = this.filesToUpload.filter((file) => {
      return file.isTranslation;
    });
  }

  public getNodeRefOfPivot(pivotId: string | undefined): string {
    const res = this.filesToUpload.find((file) => {
      return file.id === pivotId;
    });

    if (res?.nodeRef) {
      return res.nodeRef;
    }
    return '';
  }

  private async fireNotifications() {
    const nodeRefs: string[] = [];
    for (const file of this.filesToUpload) {
      if (file.nodeRef && file.uploadStatus === 'finished') {
        nodeRefs.push(file.nodeRef);
      }
    }
    try {
      await firstValueFrom(
        this.fileService.fireNewContentNotification(this.targetNodeId, nodeRefs)
      );
    } catch (error) {
      console.error(error);
    }
  }

  private getDynProp(file: FileUploadItem): { [key: string]: string } {
    const keys = Object.keys(file);
    const result: { [key: string]: string } = {};
    for (const key of keys) {
      if (key.indexOf('dynAttr') !== -1) {
        result[key] = file[key];
      }
    }
    return result;
  }

  public cancelOrClose() {
    this.canceled = true;
    this.router.navigate(['..'], { relativeTo: this.route });
  }

  private showDialogSimpleMsg() {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        messageTranslated: this.translateService.translate(
          'label.dialog.alert.snc.upload',
          {
            link: `<a href="https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en" target="_blank">C(2019)1904</a>`,
          }
        ),
        labelOK: 'label.confirm',
        title: 'label.dialog.alert.snc.upload.title',
        layoutStyle: 'SNCNotification',
      },
    });
  }

  private async cutInheritance(nodeRef: string) {
    const body: PermissionDefinition = {
      inherited: false,
      permissions: {},
    };

    await firstValueFrom(this.permissionService.putPermission(nodeRef, body));
  }
}
