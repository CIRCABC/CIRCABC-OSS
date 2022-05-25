import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';

import {
  AutoUploadConfiguration,
  AutoUploadService,
  FTPService,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { ValidationService } from 'app/core/validation.service';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-configuration',
  templateUrl: './add-configuration.component.html',
  styleUrls: ['./add-configuration.component.scss'],
  preserveWhitespaces: true,
})
export class AddConfigurationComponent implements OnInit {
  @Input()
  public showWizard = false;
  @Input()
  public igId!: string;
  @Output()
  readonly configurationAdded = new EventEmitter();
  @Output()
  readonly canceled = new EventEmitter();
  public autoUploadForm!: FormGroup;
  public connectionResult = 0;
  public testedOnce = false;
  public processing = false;
  public wizardStep = 1;
  public path: ModelNode[] = [];
  public libraryRoot!: TreeNode;
  private ig!: InterestGroup;

  constructor(
    private ftpService: FTPService,
    private autoUploadService: AutoUploadService,
    private interestGroupService: InterestGroupService,
    private formBuilder: FormBuilder,
    private nodeService: NodesService
  ) {}

  async ngOnInit() {
    await this.buildForm();
  }

  private async buildForm() {
    this.autoUploadForm = this.formBuilder.group({
      ftpHost: ['', Validators.required],
      ftpPort: ['', [Validators.required, ValidationService.portValidator]],
      pathToFile: [''],
      username: [''],
      password: [''],
      destination: ['', Validators.required],
      uploadDay: ['-1'],
      uploadHour: ['-1'],
      autoExtractZip: [false],
      jobNotifications: [false],
      emailRecipients: ['', ValidationService.emailsValidator],
    });

    await this.buildLibrarySectionTree();

    //    await this.testConnection();
  }

  private async buildLibrarySectionTree() {
    if (!this.ig) {
      this.ig = await firstValueFrom(
        this.interestGroupService.getInterestGroup(this.igId)
      );
    }

    if (this.ig && this.ig.libraryId) {
      this.libraryRoot = new TreeNode('Library', this.ig.libraryId);
      this.path = await firstValueFrom(
        this.nodeService.getPath(this.ig.libraryId)
      );
    }
  }

  public async testConnection() {
    try {
      const result = await firstValueFrom(
        this.ftpService.testFTPConnectionOnServer(
          this.autoUploadForm.controls.ftpHost.value,
          this.autoUploadForm.controls.ftpPort.value,
          this.autoUploadForm.controls.username.value,
          this.autoUploadForm.controls.password.value,
          this.autoUploadForm.controls.pathToFile.value
        )
      );
      if (result.code !== undefined) {
        this.connectionResult = result.code;
        this.testedOnce = true;
      }
    } catch (error) {
      this.connectionResult = -1;
    }
  }

  public setDestination(node: TreeNode) {
    if (node !== undefined) {
      this.autoUploadForm.controls.destination.setValue(
        node.nodeId !== this.autoUploadForm.controls.destination.value
          ? node.nodeId
          : undefined
      );
    }
  }

  public async addConfiguration() {
    try {
      this.processing = true;

      const configuration: AutoUploadConfiguration = {};

      configuration.igName = this.igId;
      configuration.ftpHost = this.autoUploadForm.controls.ftpHost.value;
      configuration.ftpPort = this.autoUploadForm.controls.ftpPort.value;
      configuration.ftpUsername = this.autoUploadForm.controls.username.value;
      configuration.ftpPassword = this.autoUploadForm.controls.password.value;
      configuration.ftpPath = this.autoUploadForm.controls.pathToFile.value;
      configuration.autoExtract =
        this.autoUploadForm.controls.autoExtractZip.value;
      configuration.jobNotifications =
        this.autoUploadForm.controls.jobNotifications.value;
      configuration.emails =
        this.autoUploadForm.controls.emailRecipients.value.replace(/\n/g, ',');

      configuration.parentId = this.autoUploadForm.controls.destination.value;

      configuration.dayChoice = this.autoUploadForm.controls.uploadDay.value;
      configuration.hourChoice = this.autoUploadForm.controls.uploadHour.value;

      await firstValueFrom(
        this.autoUploadService.postAutoUploadEntry(this.igId, configuration)
      );

      this.configurationAdded.emit();
    } finally {
      this.processing = false;
    }
    this.reset();
  }

  public destinationSelected() {
    return (
      this.autoUploadForm.controls.destination.value !== '' &&
      this.autoUploadForm.controls.destination.value !== undefined &&
      this.autoUploadForm.controls.destination.value !== null
    );
  }

  private reset() {
    this.autoUploadForm.reset();
    this.wizardStep = 1;
    this.resetConnection();
  }

  public resetConnection() {
    this.connectionResult = 0;
  }

  public nextWizardStep(): void {
    if (this.connectionResult === 1) {
      this.wizardStep += 1;
    }
  }

  public previousWizardStep(): void {
    this.wizardStep -= 1;
  }

  public canGoBack() {
    return this.wizardStep > 1 && !this.processing;
  }

  public cancelWizard() {
    this.showWizard = false;
    this.reset();
    this.canceled.emit();
    this.testedOnce = false;
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

  get destinationValue(): string {
    return this.autoUploadForm.controls.destination.value;
  }
}
