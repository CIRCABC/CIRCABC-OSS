import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  output,
  resource,
  signal,
} from '@angular/core';

import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  AutoUploadConfiguration,
  AutoUploadService,
  FTPService,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { emailsValidator, portValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeViewComponent } from 'app/shared/treeview/tree-view.component';

/**
 * Standalone Angular component that renders a multi-step wizard form for
 * creating a new auto-upload (scheduled FTP import) configuration for an
 * interest group.
 *
 * The form collects FTP connection details (host, port, credentials, remote
 * path), lets the user test that connection, pick a destination folder inside
 * the group's document library via a tree view, and configure scheduling and
 * notification options. On submission it persists the configuration through the
 * {@link AutoUploadService}.
 *
 * Key collaborators:
 * - {@link FTPService} — validates the FTP connection.
 * - {@link AutoUploadService} — persists the auto-upload configuration.
 * - {@link InterestGroupService} — resolves the interest group and its library.
 * - {@link NodesService} — resolves the library path for the tree view.
 * - {@link FormBuilder} — builds the reactive form.
 */
@Component({
  selector: 'cbc-add-configuration',
  templateUrl: './add-configuration.component.html',
  styleUrl: './add-configuration.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    ControlMessageComponent,
    TreeViewComponent,
    RouterLink,
    TranslocoModule,
  ],
})
export class AddConfigurationComponent {
  /** Service used to test the FTP connection against the server. */
  private readonly ftpService = inject(FTPService);
  /** Service used to persist the auto-upload configuration. */
  private readonly autoUploadService = inject(AutoUploadService);
  /** Service used to resolve the interest group and its library. */
  private readonly interestGroupService = inject(InterestGroupService);
  /** Factory used to build the reactive form. */
  private readonly formBuilder = inject(FormBuilder);
  /** Service used to resolve the library node path for the tree view. */
  private readonly nodeService = inject(NodesService);

  /**
   * Required input controlling whether the wizard UI is displayed.
   */
  public readonly showWizard = input.required<boolean>();
  /**
   * Required input holding the identifier of the interest group the
   * configuration is being created for.
   */
  public readonly igId = input.required<string>();
  /**
   * Emitted after a configuration has been successfully persisted, allowing the
   * parent component to refresh its view.
   */
  readonly configurationAdded = output();
  /**
   * Emitted when the user cancels the wizard, allowing the parent component to
   * dismiss it.
   */
  readonly canceled = output();
  /** Reactive form group backing the wizard's input fields. */
  public readonly autoUploadForm: FormGroup;
  /**
   * Result code of the last FTP connection test:
   * `0` = not tested, `1` = success, `-1` = error, other values map to
   * server-provided codes.
   */
  public readonly connectionResult = signal(0);
  /** Whether the FTP connection has been tested at least once. */
  public readonly testedOnce = signal(false);
  /** Whether a configuration submission is currently in progress. */
  public readonly processing = signal(false);
  /** Current step of the wizard (1-based). */
  public readonly wizardStep = signal(1);
  /**
   * Loads the interest group's library and its breadcrumb path, keyed by
   * {@link igId}. Resolves both sequential calls in a single loader and
   * returns a safe default on failure since this component has no dedicated
   * error UI for the library tree.
   */
  private readonly librarySectionResource = resource({
    params: () => this.igId(),
    loader: async ({ params: igId }) => {
      try {
        const ig = await this.interestGroupService.getInterestGroupAsync({
          id: igId,
        });

        if (!ig?.libraryId) {
          return { libraryRoot: undefined, path: [] as ModelNode[] };
        }

        const path = await this.nodeService.getPathAsync({
          id: ig.libraryId,
        });

        return {
          libraryRoot: new TreeNode('Library', ig.libraryId),
          path,
        };
      } catch (error) {
        console.error(error);
        return { libraryRoot: undefined, path: [] as ModelNode[] };
      }
    },
  });
  /** Breadcrumb path of nodes leading to the library root. */
  public readonly path = computed(
    () => this.librarySectionResource.value()?.path ?? []
  );
  /** Root tree node of the group's document library used for destination selection. */
  public readonly libraryRoot = computed(
    () => this.librarySectionResource.value()?.libraryRoot
  );

  constructor() {
    this.autoUploadForm = this.formBuilder.group({
      ftpHost: ['', Validators.required],
      ftpPort: ['', [Validators.required, portValidator]],
      pathToFile: [''],
      username: [''],
      password: [''],
      destination: ['', Validators.required],
      uploadDay: ['-1'],
      uploadHour: ['-1'],
      autoExtractZip: [false],
      jobNotifications: [false],
      emailRecipients: ['', emailsValidator],
    });
  }

  /**
   * Tests the FTP connection using the current form values and records the
   * outcome in {@link connectionResult}. Sets {@link testedOnce} on a
   * successful call. Any thrown error is caught and reported as a `-1` result.
   *
   * @returns A promise that resolves once the connection test completes.
   */
  public async testConnection() {
    try {
      const result = await this.ftpService.testFTPConnectionOnServerAsync({
        host: this.autoUploadForm.controls.ftpHost.value,
        port: this.autoUploadForm.controls.ftpPort.value,
        username: this.autoUploadForm.controls.username.value,
        password: this.autoUploadForm.controls.password.value,
        filePath: this.autoUploadForm.controls.pathToFile.value,
      });
      if (result.code !== undefined) {
        this.connectionResult.set(result.code);
        this.testedOnce.set(true);
      }
    } catch (error) {
      console.error(error);
      this.connectionResult.set(-1);
    }
  }

  /**
   * Sets the selected upload destination from the given tree node. Selecting the
   * node that is already the current destination toggles the selection off
   * (clears the destination).
   *
   * @param node The tree node representing the chosen destination folder.
   */
  public setDestination(node: TreeNode) {
    if (node !== undefined) {
      this.autoUploadForm.controls.destination.setValue(
        node.nodeId === this.autoUploadForm.controls.destination.value
          ? undefined
          : node.nodeId
      );
    }
  }

  /**
   * Builds an {@link AutoUploadConfiguration} from the current form values and
   * persists it via {@link AutoUploadService}. Toggles {@link processing} while
   * the request is in flight, emits {@link configurationAdded} on success, and
   * resets the wizard afterwards. Email recipients entered on separate lines are
   * normalised into a comma-separated list.
   *
   * @returns A promise that resolves once the configuration has been submitted.
   */
  public async addConfiguration() {
    try {
      this.processing.set(true);

      const configuration: AutoUploadConfiguration = {};

      configuration.igName = this.igId();
      configuration.ftpHost = this.autoUploadForm.controls.ftpHost.value;
      configuration.ftpPort = this.autoUploadForm.controls.ftpPort.value;
      configuration.ftpUsername = this.autoUploadForm.controls.username.value;
      configuration.ftpPassword = this.autoUploadForm.controls.password.value;
      configuration.ftpPath = this.autoUploadForm.controls.pathToFile.value;
      configuration.autoExtract =
        this.autoUploadForm.controls.autoExtractZip.value === null
          ? false
          : this.autoUploadForm.controls.autoExtractZip.value;
      configuration.jobNotifications =
        this.autoUploadForm.controls.jobNotifications.value === null
          ? false
          : this.autoUploadForm.controls.jobNotifications.value;
      configuration.emails =
        this.autoUploadForm.controls.emailRecipients.value.replaceAll(
          '\n',
          ','
        );

      configuration.parentId = this.autoUploadForm.controls.destination.value;

      configuration.dayChoice = this.autoUploadForm.controls.uploadDay.value;
      configuration.hourChoice = this.autoUploadForm.controls.uploadHour.value;

      await this.autoUploadService.postAutoUploadEntryAsync({
        id: this.igId(),
        autoUploadConfiguration: configuration,
      });

      this.configurationAdded.emit();
    } finally {
      this.processing.set(false);
    }
    this.reset();
  }

  /**
   * Indicates whether a destination folder has been selected.
   *
   * @returns `true` when the destination control holds a non-empty value.
   */
  public destinationSelected() {
    return (
      this.autoUploadForm.controls.destination.value !== '' &&
      this.autoUploadForm.controls.destination.value !== undefined &&
      this.autoUploadForm.controls.destination.value !== null
    );
  }

  /**
   * Resets the form, returns the wizard to its first step and clears the
   * connection test result.
   */
  private reset() {
    this.autoUploadForm.reset();
    this.wizardStep.set(1);
    this.resetConnection();
  }

  /** Clears the FTP connection test result back to the untested state. */
  public resetConnection() {
    this.connectionResult.set(0);
  }

  /**
   * Advances the wizard to the next step, but only when the last FTP connection
   * test succeeded.
   */
  public nextWizardStep(): void {
    if (this.connectionResult() === 1) {
      this.wizardStep.update((step) => step + 1);
    }
  }

  /** Moves the wizard back to the previous step. */
  public previousWizardStep(): void {
    this.wizardStep.update((step) => step - 1);
  }

  /**
   * Indicates whether the user may navigate to a previous wizard step.
   *
   * @returns `true` when past the first step and not currently processing.
   */
  public canGoBack() {
    return this.wizardStep() > 1 && !this.processing();
  }

  /**
   * Cancels the wizard: resets the form, emits {@link canceled} and clears the
   * tested flag.
   */
  public cancelWizard() {
    this.reset();
    this.canceled.emit();
    this.testedOnce.set(false);
  }

  /**
   * The FTP host form control, used by the template to display validation
   * messages.
   *
   * @returns The `ftpHost` control.
   */
  get ftpHostControl(): AbstractControl {
    return this.autoUploadForm.controls.ftpHost;
  }

  /**
   * The FTP port form control, used by the template to display validation
   * messages.
   *
   * @returns The `ftpPort` control.
   */
  get ftpPortControl(): AbstractControl {
    return this.autoUploadForm.controls.ftpPort;
  }

  /**
   * The email recipients form control, used by the template to display
   * validation messages.
   *
   * @returns The `emailRecipients` control.
   */
  get emailRecipientsControl(): AbstractControl {
    return this.autoUploadForm.controls.emailRecipients;
  }

  /**
   * The currently selected destination node identifier.
   *
   * @returns The value of the `destination` control.
   */
  get destinationValue(): string {
    return this.autoUploadForm.controls.destination.value;
  }
}
