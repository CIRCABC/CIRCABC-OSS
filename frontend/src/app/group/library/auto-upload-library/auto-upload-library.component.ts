import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ActionType } from 'app/action-result';
import {
  AutoUploadConfiguration,
  AutoUploadService,
  FTPService,
} from 'app/core/generated/circabc';
import { LoadingService } from 'app/core/loading.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getSuccessTranslation } from 'app/core/util';
import { emailsValidator, portValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component that renders the auto-upload configuration
 * screen for a document in an interest group library.
 *
 * The template exposes a reactive form allowing an administrator to configure
 * an FTP source from which a file is periodically fetched and uploaded into the
 * library node, together with scheduling (day/hour), ZIP auto-extraction and
 * e-mail notification options. It also lets the user test the FTP connection,
 * enable/disable an existing configuration, save it and reset the form.
 *
 * The interest group id (`igId`) and the target library node id (`nodeId`) are
 * read from the route parameters.
 *
 * Key collaborators:
 * - {@link FTPService} to test the FTP connection.
 * - {@link AutoUploadService} to read, create/update and toggle the
 *   auto-upload configuration entry.
 * - {@link TranslocoService} to translate user-facing success messages.
 * - {@link UiMessageService} to display success notifications.
 */
@Component({
  selector: 'cbc-library-auto-upload',
  templateUrl: './auto-upload-library.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    SpinnerComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class AutoUploadLibraryComponent implements OnInit {
  /** Current activated route, used to read the `id` and `nodeId` parameters. */
  private readonly route = inject(ActivatedRoute);
  /** API client used to test the FTP connection on the server. */
  private readonly ftpService = inject(FTPService);
  /** API client used to read, persist and toggle the auto-upload entry. */
  private readonly autoUploadService = inject(AutoUploadService);
  /** Service used to translate success message keys into localized text. */
  private readonly translateService = inject(TranslocoService);
  /** Service used to display success notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Factory used to build the reactive auto-upload form. */
  private readonly formBuilder = inject(FormBuilder);
  private readonly loadingService = inject(LoadingService);

  /** Auto-upload configuration currently loaded from / persisted to the backend. */
  public readonly configuration = signal<AutoUploadConfiguration>(
    {} as AutoUploadConfiguration
  );
  /** Identifier of the library node the auto-upload targets. */
  public nodeId!: string;
  /** Identifier of the interest group that owns the node. */
  public igId!: string;

  /** Reactive form backing the auto-upload configuration inputs. */
  public autoUploadForm!: FormGroup;

  /** Whether the configuration is currently being loaded from the backend. */
  public readonly loading = signal(false);
  /** Whether an asynchronous action (test, save, toggle) is in progress. */
  public readonly processing = signal(false);

  /**
   * Result code of the last FTP connection test. A value greater than 0
   * indicates a successful connection and enables saving the configuration.
   */
  public readonly connectionResult = signal(0);

  /**
   * Angular lifecycle hook. Builds the reactive form, subscribes to the route
   * parameters to capture `igId`/`nodeId` and loads the existing
   * configuration for the resolved node.
   */
  ngOnInit() {
    this.autoUploadForm = this.formBuilder.group(
      {
        ftpHost: ['', Validators.required],
        ftpPort: ['', [Validators.required, portValidator]],
        pathToFile: [''],
        username: [''],
        password: [''],
        uploadDay: ['-1'],
        uploadHour: ['-1'],
        autoExtractZip: [false],
        jobNotifications: [false],
        emailRecipients: ['', emailsValidator],
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

  /**
   * Loads the auto-upload configuration for the current interest group and
   * node from the backend and patches the form controls with the retrieved
   * values. When no configuration exists yet, the form is left with its
   * default values. Toggles {@link loading} around the request.
   *
   * @returns A promise that resolves once the configuration has been loaded
   *          and the form has been populated.
   */
  private async loadConfiguration() {
    if (this.igId === undefined || this.nodeId === undefined) {
      return;
    }
    const configuration = await this.loadingService.run(this.loading, () =>
      this.autoUploadService.getAutoUploadEntryAsync({
        id: this.igId,
        nodeId: this.nodeId,
      })
    );
    if (configuration === undefined) {
      return;
    }
    this.configuration.set(configuration);
    if (Object.keys(this.configuration()).length !== 0) {
      this.autoUploadForm.controls.ftpHost.patchValue(
        this.configuration().ftpHost
      );
      this.autoUploadForm.controls.ftpPort.patchValue(
        this.configuration().ftpPort
      );
      this.autoUploadForm.controls.pathToFile.patchValue(
        this.configuration().ftpPath
      );
      this.autoUploadForm.controls.username.patchValue(
        this.configuration().ftpUsername
      );
      this.autoUploadForm.controls.uploadDay.patchValue(
        this.configuration().dayChoice
      );
      this.autoUploadForm.controls.uploadHour.patchValue(
        this.configuration().hourChoice
      );
      this.autoUploadForm.controls.autoExtractZip.patchValue(
        this.configuration().autoExtract
      );
      this.autoUploadForm.controls.jobNotifications.patchValue(
        this.configuration().jobNotifications
      );
      this.autoUploadForm.controls.emailRecipients.patchValue(
        this.parseEmails(this.configuration().emails)
      );
    }
  }

  /**
   * Converts the comma-separated e-mail list returned by the backend into a
   * newline-separated string suitable for display in a multi-line text field.
   *
   * @param emails Comma-separated list of e-mail addresses, or `undefined`.
   * @returns The newline-separated list, or `undefined` when no input was given.
   */
  private parseEmails(emails: string | undefined): string | undefined {
    if (emails === undefined) {
      return undefined;
    }
    return emails.replaceAll(',', '\n');
  }

  /**
   * Tests the FTP connection using the host, port, credentials and path
   * currently entered in the form. Stores the returned result code in
   * {@link connectionResult}; a positive code enables saving. Toggles
   * {@link processing} around the request.
   *
   * @returns A promise that resolves once the connection test has completed.
   */
  public async testConnection() {
    this.processing.set(true);
    const result = await this.ftpService.testFTPConnectionOnServerAsync({
      host: this.autoUploadForm.controls.ftpHost.value,
      port: this.autoUploadForm.controls.ftpPort.value,
      username: this.autoUploadForm.controls.username.value,
      password: this.autoUploadForm.controls.password.value,
      filePath: this.autoUploadForm.controls.pathToFile.value,
    });
    if (result.code) {
      this.connectionResult.set(result.code);
    }
    this.processing.set(false);
  }

  /**
   * Enables or disables the existing auto-upload configuration. When the
   * configuration status is not `2`, it flips the enabled state (based on a
   * current status of `0`) via the backend and reloads the configuration.
   * Toggles {@link processing} around the request.
   *
   * @returns A promise that resolves once the toggle and reload have completed.
   */
  public async toggleConfiguration() {
    this.processing.set(true);
    if (this.configuration().status !== 2) {
      try {
        await this.autoUploadService.putAutoUploadEntryAsync({
          id: this.igId,
          configurationId: String(this.configuration().idConfiguration),
          enable: this.configuration().status === 0,
        });
        await this.loadConfiguration();
      } finally {
        this.processing.set(false);
      }
    }
    this.processing.set(false);
  }

  /**
   * Persists the auto-upload configuration built from the current form values.
   * Does nothing unless a successful connection test has been performed
   * ({@link connectionResult} greater than 0). On success, displays a
   * localized notification and reloads the configuration. Toggles
   * {@link processing} around the request.
   *
   * @param action Either `'add'` for a new configuration or any other value
   *               for an update; controls which success message is shown.
   * @returns A promise that resolves once the configuration has been saved and
   *          reloaded.
   */
  public async save(action: string) {
    if (this.connectionResult() <= 0) {
      return;
    }

    try {
      this.processing.set(true);

      const configuration = this.configuration();
      if (configuration.idConfiguration === undefined) {
        configuration.idConfiguration = 0;
      } else {
        configuration.idConfiguration = Number(configuration.idConfiguration);
      }
      configuration.igName = this.igId;
      configuration.ftpHost = this.autoUploadForm.controls.ftpHost.value;
      configuration.ftpPort = Number(
        this.autoUploadForm.controls.ftpPort.value
      );
      configuration.ftpPath = this.autoUploadForm.controls.pathToFile.value;
      configuration.ftpUsername = this.autoUploadForm.controls.username.value;
      configuration.ftpPassword = this.autoUploadForm.controls.password.value;
      configuration.autoExtract =
        this.autoUploadForm.controls.autoExtractZip.value;
      configuration.jobNotifications =
        this.autoUploadForm.controls.jobNotifications.value;
      configuration.emails =
        this.autoUploadForm.controls.emailRecipients.value.replaceAll(
          '\n',
          ','
        );

      configuration.fileId = this.nodeId;

      configuration.dayChoice = Number(
        this.autoUploadForm.controls.uploadDay.value
      );
      configuration.hourChoice = Number(
        this.autoUploadForm.controls.uploadHour.value
      );

      await this.autoUploadService.postAutoUploadEntryAsync({
        id: this.igId,
        autoUploadConfiguration: configuration,
      });

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
      this.processing.set(false);
    }
  }

  /**
   * Resets the connection test result and reloads the configuration from the
   * backend, discarding any unsaved form changes.
   *
   * @returns A promise that resolves once the configuration has been reloaded.
   */
  public async resetForm() {
    this.connectionResult.set(0);
    await this.loadConfiguration();
  }

  /** Reactive form control holding the FTP host value. */
  get ftpHostControl(): AbstractControl {
    return this.autoUploadForm.controls.ftpHost;
  }

  /** Reactive form control holding the FTP port value. */
  get ftpPortControl(): AbstractControl {
    return this.autoUploadForm.controls.ftpPort;
  }
  /** Reactive form control holding the newline-separated e-mail recipients. */
  get emailRecipientsControl(): AbstractControl {
    return this.autoUploadForm.controls.emailRecipients;
  }

  /**
   * Indicates whether the current view edits an existing configuration
   * (`true`) rather than creating a new one (`false`).
   */
  get isUpdate(): boolean {
    return this.configuration()?.idConfiguration !== undefined;
  }
}
