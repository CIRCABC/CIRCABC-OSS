import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { TranslocoService } from '@ngneat/transloco';
import { ActionType } from 'app/action-result';
import {
  AutoUploadConfiguration,
  AutoUploadService,
  FTPService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getSuccessTranslation } from 'app/core/util';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-library-auto-upload',
  templateUrl: './auto-upload-library.component.html',
  preserveWhitespaces: true,
})
export class AutoUploadLibraryComponent implements OnInit {
  public configuration!: AutoUploadConfiguration;
  public nodeId!: string;
  public igId!: string;

  public autoUploadForm!: FormGroup;

  public loading = false;
  public processing = false;

  public connectionResult = 0;

  constructor(
    private route: ActivatedRoute,
    private ftpService: FTPService,
    private autoUploadService: AutoUploadService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private formBuilder: FormBuilder
  ) {}

  ngOnInit() {
    this.autoUploadForm = this.formBuilder.group(
      {
        ftpHost: ['', Validators.required],
        ftpPort: ['', [Validators.required, ValidationService.portValidator]],
        pathToFile: [''],
        username: [''],
        password: [''],
        uploadDay: ['-1'],
        uploadHour: ['-1'],
        autoExtractZip: [false],
        jobNotifications: [false],
        emailRecipients: ['', ValidationService.emailsValidator],
      },
      {
        updateOn: 'change',
      }
    );

    this.route.params.subscribe(async (params) => {
      this.igId = params.id;
      this.nodeId = params.nodeId;
      await this.loadConfiguration();
    });
  }

  private async loadConfiguration() {
    this.loading = true;
    if (this.igId !== undefined && this.nodeId !== undefined) {
      this.configuration = await firstValueFrom(
        this.autoUploadService.getAutoUploadEntry(this.igId, this.nodeId)
      );
      if (Object.keys(this.configuration).length !== 0) {
        this.autoUploadForm.controls.ftpHost.patchValue(
          this.configuration.ftpHost
        );
        this.autoUploadForm.controls.ftpPort.patchValue(
          this.configuration.ftpPort
        );
        this.autoUploadForm.controls.pathToFile.patchValue(
          this.configuration.ftpPath
        );
        this.autoUploadForm.controls.username.patchValue(
          this.configuration.ftpUsername
        );
        this.autoUploadForm.controls.uploadDay.patchValue(
          this.configuration.dayChoice
        );
        this.autoUploadForm.controls.uploadHour.patchValue(
          this.configuration.hourChoice
        );
        this.autoUploadForm.controls.autoExtractZip.patchValue(
          this.configuration.autoExtract
        );
        this.autoUploadForm.controls.jobNotifications.patchValue(
          this.configuration.jobNotifications
        );
        this.autoUploadForm.controls.emailRecipients.patchValue(
          this.parseEmails(this.configuration.emails)
        );
      }
    }
    this.loading = false;
  }

  private parseEmails(emails: string | undefined): string | undefined {
    if (emails === undefined) {
      return undefined;
    }
    return emails.replace(/,/g, '\n');
  }

  public async testConnection() {
    this.processing = true;
    const result = await firstValueFrom(
      this.ftpService.testFTPConnectionOnServer(
        this.autoUploadForm.controls.ftpHost.value,
        this.autoUploadForm.controls.ftpPort.value,
        this.autoUploadForm.controls.username.value,
        this.autoUploadForm.controls.password.value,
        this.autoUploadForm.controls.pathToFile.value
      )
    );
    if (result.code) {
      this.connectionResult = result.code;
    }
    this.processing = false;
  }

  public async toggleConfiguration() {
    this.processing = true;
    if (this.configuration.status !== 2) {
      try {
        await firstValueFrom(
          this.autoUploadService.putAutoUploadEntry(
            this.igId,
            String(this.configuration.idConfiguration),
            this.configuration.status === 0
          )
        );
        await this.loadConfiguration();
      } finally {
        this.processing = false;
      }
    }
    this.processing = false;
  }

  public async save(action: string) {
    if (this.connectionResult <= 0) {
      return;
    }

    try {
      this.processing = true;

      if (this.configuration.idConfiguration === undefined) {
        this.configuration.idConfiguration = 0;
      } else {
        this.configuration.idConfiguration = Number(
          this.configuration.idConfiguration
        );
      }
      this.configuration.igName = this.igId;
      this.configuration.ftpHost = this.autoUploadForm.controls.ftpHost.value;
      this.configuration.ftpPort = Number(
        this.autoUploadForm.controls.ftpPort.value
      );
      this.configuration.ftpPath =
        this.autoUploadForm.controls.pathToFile.value;
      this.configuration.ftpUsername =
        this.autoUploadForm.controls.username.value;
      this.configuration.ftpPassword =
        this.autoUploadForm.controls.password.value;
      this.configuration.autoExtract =
        this.autoUploadForm.controls.autoExtractZip.value;
      this.configuration.jobNotifications =
        this.autoUploadForm.controls.jobNotifications.value;
      this.configuration.emails =
        this.autoUploadForm.controls.emailRecipients.value.replace(/\n/g, ',');

      this.configuration.fileId = this.nodeId;

      this.configuration.dayChoice = Number(
        this.autoUploadForm.controls.uploadDay.value
      );
      this.configuration.hourChoice = Number(
        this.autoUploadForm.controls.uploadHour.value
      );

      await firstValueFrom(
        this.autoUploadService.postAutoUploadEntry(
          this.igId,
          this.configuration
        )
      );

      const text = this.translateService.translate(
        getSuccessTranslation(
          action === 'add'
            ? ActionType.ADD_AUTOUPLOAD
            : ActionType.UPDATE_AUTOUPLOAD
        )
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }

      await this.loadConfiguration();
    } finally {
      this.processing = false;
    }
  }

  public async resetForm() {
    this.connectionResult = 0;
    await this.loadConfiguration();
  }

  get ftpHostControl(): AbstractControl {
    return this.autoUploadForm.controls.ftpHost;
  }

  get ftpPortControl(): AbstractControl {
    return this.autoUploadForm.controls.ftpPort;
  }
  get emailRecipientsControl(): AbstractControl {
    return this.autoUploadForm.controls.emailRecipients;
  }

  get isUpdate(): boolean {
    return (
      this.configuration !== undefined &&
      this.configuration.idConfiguration !== undefined
    );
  }
}
