import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ContentService,
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  Node as ModelNode,
  NodesService,
  PermissionDefinition,
  PermissionService,
  SpaceService,
} from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { fileNameValidator } from 'app/core/validation.service';
import { EncodingInputComponent } from 'app/group/library/input/encoding-input.component';
import { MimetypeInputComponent } from 'app/group/library/input/mimetype-input.component';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { environment } from 'environments/environment';
import { firstValueFrom, Subscription } from 'rxjs';

/**
 * Standalone Angular component that renders the edit form for a library node
 * (a file, a link or a folder) inside an interest group.
 *
 * The component displays a tabbed reactive form covering general information
 * (name, multilingual title/description, author, URL, status, encoding,
 * mimetype, reference, security ranking, issue/expiration dates), node details
 * and a dynamically built set of group-specific dynamic properties. On submit
 * it persists the changes back to the backend and navigates to the node
 * details view.
 *
 * Key collaborators:
 * - {@link NodesService} to load the edited node.
 * - {@link ContentService} / {@link SpaceService} to persist file / folder
 *   changes respectively.
 * - {@link DynamicPropertiesService} to retrieve the group's dynamic property
 *   definitions.
 * - {@link PermissionService} to cut permission inheritance for sensitive
 *   content (ECHA release).
 * - {@link TranslocoService} for the active UI language and translated dialog
 *   messages.
 * - {@link MatDialog} to display the sensitive-content confirmation dialog.
 *
 * The `id` (group id) and `nodeId` route parameters drive which node and
 * dynamic property model are loaded.
 */
@Component({
  selector: 'cbc-edit-node',
  templateUrl: './edit-node.component.html',
  styleUrl: './edit-node.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RichTextEditorComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    MultilingualInputComponent,
    MatSlideToggleModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    MimetypeInputComponent,
    EncodingInputComponent,
    RouterLink,
    I18nPipe,
    TranslocoModule,
  ],
})
export class EditNodeComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly nodesService = inject(NodesService);
  private readonly spaceService = inject(SpaceService);
  private readonly contentService = inject(ContentService);
  private readonly translateService = inject(TranslocoService);
  private readonly dynamicPropertiesService = inject(DynamicPropertiesService);
  private readonly permissionService = inject(PermissionService);
  private readonly dialog = inject(MatDialog);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  /** The node (file, link or folder) currently being edited. */
  public node!: ModelNode;
  /** Reactive form backing the edit UI; built in {@link ngOnInit}. */
  public editNodeForm!: FormGroup;
  /**
   * Definitions of the group's dynamic properties, used to render extra form
   * controls named `dynAttr<index>`.
   */
  public dynamicPropertiesModel: DynamicPropertyDefinition[] = [];
  /**
   * Whether group members should be notified of the change; bound to a
   * notification toggle and passed to the save calls.
   */
  public notify = new FormControl(true, { nonNullable: true });
  /** Identifier of the currently active tab in the edit view. */
  public selectedTab = 'GeneralInformation';
  /** Subscription managing calendar date handling for the expiration date. */
  private dateSubscription!: Subscription;

  /**
   * Angular lifecycle hook. Builds the reactive form, wires up calendar date
   * handling for the expiration date, applies release-specific behaviour
   * (disables the expiration toggle for the `olaf` release) and, for each
   * route parameter change, loads the dynamic property model and the node.
   */
  public ngOnInit() {
    this.editNodeForm = this.fb.group(
      {
        name: ['', [Validators.required, fileNameValidator]],
        title: [{ EN: '' }],
        description: [{ EN: '' }],
        author: [],
        url: [],
        status: [],
        encoding: [],
        mimetype: [],
        reference: [],
        security: [],
        issueDate: [],
        expirationDate: [],
        expirationDateActived: [],
      },
      {
        updateOn: 'change',
      }
    );

    this.dateSubscription = setupCalendarDateHandling(
      this.editNodeForm.controls.expirationDate
    );

    if (environment.circabcRelease === 'olaf') {
      this.editNodeForm.controls.expirationDateActived.disable();
    }

    this.route.params.subscribe(async (params) => {
      await this.loadDynamicPropertiesModel(params.id);
      await this.loadNode(params.nodeId);
      // OnPush: node and dynamicPropertiesModel are populated from this async
      // route callback, so mark the view dirty to render the loaded data.
      this.changeDetectorRef.markForCheck();
    });
  }

  /**
   * Angular lifecycle hook. Cleans up the calendar date subscription to avoid
   * memory leaks when the component is destroyed.
   */
  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

  /**
   * Loads the dynamic property definitions for the given group and adds a
   * matching form control (named `dynAttr<index>`) for each definition.
   *
   * @param groupId Identifier of the group whose dynamic property definitions
   *   should be loaded. When falsy, nothing is loaded.
   * @returns A promise that resolves once the model and controls are set up.
   */
  public async loadDynamicPropertiesModel(groupId: string) {
    if (groupId) {
      this.dynamicPropertiesModel =
        await this.dynamicPropertiesService.getDynamicPropertyDefinitionsAsync({
          id: groupId,
        });

      for (const dynprop of this.dynamicPropertiesModel) {
        this.editNodeForm.addControl(this.getName(dynprop), new FormControl());
      }
    }
  }

  /**
   * Fetches the node to edit and, when it has properties, populates the form's
   * basic fields, the expiration-date flag and the dynamic properties.
   *
   * @param nodeId Identifier of the node to load.
   * @returns A promise that resolves once the node is loaded and the form is
   *   populated.
   */
  public async loadNode(nodeId: string) {
    this.node = await this.nodesService.getNodeAsync({ id: nodeId });

    if (this.node.properties) {
      this.populateBasicFields();
      this.populateExpirationDateFlag();
      this.populateDynamicProperties();
    }
  }

  /**
   * Copies the loaded node's core metadata into the form controls (name,
   * title, description, author, optional URL for links, status, encoding,
   * mimetype, reference, security ranking and issue/expiration dates).
   */
  private populateBasicFields() {
    const props = this.node.properties;
    if (!props) return;

    const controls = this.editNodeForm.controls;

    controls.name.setValue(this.node.name);
    controls.title.setValue(this.node.title);
    controls.description.setValue(this.node.description);
    controls.author.setValue(props.author || '');

    if (this.isLink()) {
      controls.url.setValue(props.url || '');
    }

    controls.status.setValue(props.status);
    controls.encoding.setValue(props.encoding);
    controls.mimetype.setValue(props.mimetype);
    controls.reference.setValue(props.reference || '');
    controls.security.setValue(props.security_ranking);
    controls.issueDate.setValue(
      props.issue_date ? new Date(props.issue_date) : undefined
    );
    controls.expirationDate.setValue(
      props.expiration_date ? new Date(props.expiration_date) : undefined
    );
  }

  /**
   * Sets the `expirationDateActived` toggle based on whether an expiration
   * date is currently present in the form.
   */
  private populateExpirationDateFlag() {
    const expDate = this.editNodeForm.value.expirationDate;
    this.editNodeForm.controls.expirationDateActived.setValue(!!expDate);
  }

  /**
   * Populates the dynamic property form controls from the node's properties.
   * Skipped for folders. Handles selection/multi-selection, date fields and
   * plain values distinctly.
   */
  private populateDynamicProperties() {
    if (this.node.type?.includes('folder')) return;

    const props = this.node.properties;
    if (!props) return;

    for (const dynprop of this.dynamicPropertiesModel) {
      const controlName = this.getName(dynprop);

      if (this.isSelectionOrMultiSelection(dynprop)) {
        this.setSelectionValue(controlName, props[controlName], dynprop);
      } else if (this.isDateField(dynprop) && props[controlName]) {
        this.editNodeForm.controls[controlName].setValue(
          new Date(props[controlName])
        );
      } else {
        this.editNodeForm.controls[controlName].setValue(
          props[controlName] || ''
        );
      }
    }
  }

  /**
   * Sets the value of a selection or multi-selection dynamic property control.
   * Single selections are assigned as-is; multi-selections are converted from a
   * comma-separated string to an array.
   *
   * @param controlName Name of the target form control.
   * @param value Raw value read from the node properties.
   * @param dynprop The dynamic property definition describing the control.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private setSelectionValue(controlName: string, value: any, dynprop: any) {
    if (this.isSelection(dynprop)) {
      this.editNodeForm.controls[controlName].setValue(value);
    } else {
      this.editNodeForm.controls[controlName].setValue(
        this.convertToArray(value)
      );
    }
  }

  /**
   * Converts a comma-separated string into an array of trimmed values.
   *
   * @param value Comma-and-space separated string (e.g. `"a, b, c"`).
   * @returns The parsed array, or an empty array when the input is falsy.
   */
  private convertToArray(value: string): string[] {
    if (!value) return [];
    return value.split(', ');
  }

  /**
   * Indicates whether the edited node is a file (as opposed to a folder).
   *
   * @returns `true` when the node's type does not include `folder`.
   */
  public isFile(): boolean {
    return !this.node?.type?.includes('folder');
  }

  /**
   * Indicates whether the edited node represents a link.
   *
   * @returns `true` when the node has HTML mimetype and a non-empty URL.
   */
  public isLink(): boolean {
    if (this.node?.type && this.node.properties) {
      return (
        this.node.properties.mimetype === 'text/html' &&
        this.node.properties.url !== ''
      );
    }
    return false;
  }

  /**
   * The currently active UI language.
   *
   * @returns The active Transloco language code.
   */
  public get currentLanguage(): string {
    return this.translateService.getActiveLang();
  }

  /**
   * Submits the form and persists the node changes.
   *
   * Aborts early if the node is expired while the expiration toggle is set, or
   * if the sensitive-content confirmation is declined. Otherwise builds the
   * node model, saves it, optionally cuts permission inheritance and navigates
   * to the node details view.
   *
   * @returns A promise that resolves once the update flow completes.
   */
  public async updateProperties() {
    if (this.isExpired() && this.editNodeForm.controls.expirationDateActived) {
      return;
    }

    if (!(await this.checkSecurityConfirmation())) {
      return;
    }

    const tmpNode = this.buildNodeModel();
    await this.saveNode(tmpNode);
    await this.handleSecurityCutInheritance();

    this.router.navigate(['../details'], { relativeTo: this.route });
  }

  /**
   * Determines whether the user must confirm before saving sensitive content.
   * For the `echa` release, sensitive or special-handling security rankings
   * trigger a confirmation dialog.
   *
   * @returns A promise resolving to `true` when saving may proceed, `false`
   *   when the user declined the confirmation.
   */
  private async checkSecurityConfirmation(): Promise<boolean> {
    const isSensitive =
      this.editNodeForm.value.security === 'SENSITIVE' ||
      this.editNodeForm.value.security === 'SPECIAL_HANDLING';

    if (environment.circabcRelease === 'echa' && isSensitive) {
      return await this.showDialogConfirmMsg();
    }
    return true;
  }

  /**
   * Builds the {@link ModelNode} payload from the current form values, choosing
   * file or folder property shapes depending on the node type.
   *
   * @returns The node model to be persisted.
   */
  private buildNodeModel(): ModelNode {
    const isFile = this.isFile();

    return {
      id: this.node.id,
      name: this.editNodeForm.value.name,
      title: this.editNodeForm.value.title,
      description: this.editNodeForm.value.description,
      properties: isFile
        ? this.buildFileProperties()
        : this.buildFolderProperties(),
    };
  }

  /**
   * Builds the property object for a file/link node from the form values,
   * including dynamic properties when the node is not a folder.
   *
   * @returns The assembled file properties object.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private buildFileProperties(): any {
    const props = {
      expiration_date: this.getExpirationDate(),
      issue_date: this.editNodeForm.value.issueDate || '',
      encoding: this.editNodeForm.value.encoding || '',
      mimetype: this.editNodeForm.value.mimetype?.mimetype || '',
      reference: this.editNodeForm.value.reference || '',
      author: this.editNodeForm.value.author || '',
      url: this.editNodeForm.value.url || '',
      status: this.editNodeForm.value.status || 'DRAFT',
      security: this.editNodeForm.value.security || '',
    };

    if (!this.node.type?.includes('folder')) {
      this.addDynamicProperties(props);
    }

    return props;
  }

  /**
   * Builds the property object for a folder node, containing only the
   * expiration date.
   *
   * @returns The assembled folder properties object.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private buildFolderProperties(): any {
    return {
      expiration_date: this.getExpirationDate(),
    };
  }

  /**
   * Resolves the expiration date to persist. For the `olaf` release the raw
   * date is always used; otherwise the date is only kept when the expiration
   * toggle is active, and `null` is returned when disabled.
   *
   * @returns The expiration date value to save, or `null`.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private getExpirationDate(): any {
    const expDate = this.editNodeForm.value.expirationDate;
    const isActive = this.editNodeForm.value.expirationDateActived;

    if (environment.circabcRelease === 'olaf') {
      return expDate;
    }

    return expDate && isActive ? expDate : null;
  }

  /**
   * Appends the dynamic property values to the given properties object,
   * joining array values into comma-separated strings.
   *
   * @param props The properties object to augment in place.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private addDynamicProperties(props: any) {
    for (const dynprop of this.dynamicPropertiesModel) {
      const controlName = this.getName(dynprop);
      const value = this.editNodeForm.value[controlName];
      props[controlName] = Array.isArray(value)
        ? value.join(', ')
        : value || '';
    }
  }

  /**
   * Persists the node using the appropriate backend service: {@link
   * ContentService} for files/links and {@link SpaceService} for folders.
   *
   * @param tmpNode The node model to save.
   * @returns A promise that resolves once the save request completes.
   */
  private async saveNode(tmpNode: ModelNode) {
    if (this.isFile()) {
      await this.contentService.putContentAsync({
        id: this.node.id as string,
        node: tmpNode,
        notify: this.notify.value,
      });
    } else {
      await this.spaceService.putSpaceAsync({
        id: this.node.id as string,
        node: tmpNode,
        notify: this.notify.value,
      });
    }
  }

  /**
   * For the `echa` release, cuts permission inheritance on the saved node when
   * its security ranking is sensitive or special-handling.
   *
   * @returns A promise that resolves once inheritance handling completes.
   */
  private async handleSecurityCutInheritance() {
    const isSensitive =
      this.editNodeForm.value.security === 'SENSITIVE' ||
      this.editNodeForm.value.security === 'SPECIAL_HANDLING';

    if (environment.circabcRelease === 'echa' && isSensitive && this.node.id) {
      await this.cutInheritance(this.node.id);
    }
  }

  /**
   * @returns `true` when the General Information tab is selected.
   */
  public isGeneralTab(): boolean {
    return this.selectedTab === 'GeneralInformation';
  }

  /**
   * @returns `true` when the Details tab is selected.
   */
  public isDetailsTab(): boolean {
    return this.selectedTab === 'Details';
  }

  /**
   * @returns `true` when the Dynamic Properties tab is selected.
   */
  public isDynamicPropertiesTab(): boolean {
    return this.selectedTab === 'DynamicProperties';
  }

  /**
   * Switches the active tab.
   *
   * @param tab Identifier of the tab to activate.
   */
  public setTab(tab: string) {
    this.selectedTab = tab;
  }

  /** The `name` form control. */
  get nameControl(): AbstractControl {
    return this.editNodeForm.controls.name;
  }

  /** The `author` form control. */
  get authorControl(): AbstractControl {
    return this.editNodeForm.controls.author;
  }

  /** The `url` form control. */
  get urlControl(): AbstractControl {
    return this.editNodeForm.controls.url;
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a date field.
   */
  public isDateField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'DATE_FIELD';
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a single-line text field.
   */
  public isTextField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'TEXT_FIELD';
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a multi-line text area.
   */
  public isTextArea(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'TEXT_AREA';
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a selection or multi-selection.
   */
  public isSelectionOrMultiSelection(dpd: DynamicPropertyDefinition): boolean {
    return (
      dpd.propertyType === 'SELECTION' || dpd.propertyType === 'MULTI_SELECTION'
    );
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a multi-selection.
   */
  public isMultiSelection(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'MULTI_SELECTION';
  }

  /**
   * @param dpd The dynamic property definition to test.
   * @returns `true` when the property is a single selection.
   */
  public isSelection(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'SELECTION';
  }

  /**
   * @param dpd The dynamic property definition to read.
   * @returns The definition's index.
   */
  public getIndex(dpd: DynamicPropertyDefinition): number {
    return dpd.index as number;
  }

  /**
   * Builds the form control name for a dynamic property.
   *
   * @param dynprop The dynamic property definition.
   * @returns The control name in the form `dynAttr<index>`.
   */
  private getName(dynprop: DynamicPropertyDefinition): string {
    return `dynAttr${dynprop.index}`;
  }

  /**
   * Determines whether a given option is currently selected for a dynamic
   * property control.
   *
   * @param prop The option value to check.
   * @param dynprop The dynamic property definition whose control is inspected.
   * @returns `true` when the control's value contains the option.
   */
  public isSelectedOption(
    prop: string,
    dynprop: DynamicPropertyDefinition
  ): boolean {
    const controlValue = this.editNodeForm.value[`dynAttr${dynprop.index}`];
    if (controlValue) {
      return controlValue.includes(prop);
    }
    return false;
  }

  /**
   * Comparison function used by selection controls to match option values.
   *
   * @param optionOne First value to compare.
   * @param optionTwo Second value to compare.
   * @returns `true` when both values are defined and equal.
   */
  public compareFn(optionOne?: string, optionTwo?: string): boolean {
    if (optionOne && optionTwo) {
      return optionOne === optionTwo;
    }
    return false;
  }

  /**
   * Indicates whether the node is expired.
   *
   * @returns `true` when the expiration toggle is active and the expiration
   *   date is in the past.
   */
  public isExpired() {
    if (
      this.editNodeForm.controls.expirationDateActived.value &&
      this.editNodeForm.controls.expirationDate.value < Date.now()
    ) {
      return true;
    }
    return false;
  }

  /**
   * Indicates whether an expiration date is required but missing.
   *
   * @returns `true` when the expiration toggle is active but no expiration date
   *   has been provided.
   */
  public expirationDateRequired() {
    return (
      this.editNodeForm.controls.expirationDateActived.value &&
      (this.editNodeForm.controls.expirationDate.value === null ||
        this.editNodeForm.controls.expirationDate.value === undefined)
    );
  }

  /**
   * Removes permission inheritance on the given node by setting an explicit,
   * non-inherited empty permission definition.
   *
   * @param nodeRef Reference of the node whose inheritance should be cut.
   * @returns A promise that resolves once the permission update completes.
   */
  private async cutInheritance(nodeRef: string) {
    const body: PermissionDefinition = {
      inherited: false,
      permissions: {},
    };

    await this.permissionService.putPermissionAsync({
      id: nodeRef,
      permissionDefinition: body,
    });
  }

  /**
   * Opens the sensitive-content confirmation dialog (used for the `echa`
   * release) and awaits the user's decision.
   *
   * @returns A promise resolving to the dialog result: `true` when confirmed,
   *   otherwise a falsy value.
   */
  private async showDialogConfirmMsg() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
      data: {
        messageTranslated: this.translateService.translate(
          'label.dialog.alert.snc.edit',
          {
            link: `<a href="https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en" target="_blank">C(2019)1904</a>`,
          }
        ),
        labelOK: 'label.confirm',
        title: 'label.dialog.alert.snc.edit.title',
        layoutStyle: 'SNCNotification',
      },
    });

    return firstValueFrom(dialogRef.afterClosed());
  }
}
