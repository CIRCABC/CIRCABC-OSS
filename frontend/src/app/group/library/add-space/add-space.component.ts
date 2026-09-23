import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  OnChanges,
  OnDestroy,
  output,
  SimpleChanges,
  signal,
  viewChild,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import {
  Node as ModelNode,
  PagedNodes,
  PermissionDefinition,
  PermissionService,
  SpaceService,
} from 'app/core/generated/circabc';
import { removeNulls } from 'app/core/util';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import {
  fileFolderExistsValidator,
  nameValidator,
} from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { HintComponent } from 'app/shared/hint/hint.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { Subscription } from 'rxjs';

/**
 * Modal wizard component used within the document library to create a new
 * sub-space (folder) under a given parent node.
 *
 * The component renders a modal dialog (via {@link ModalComponent}) hosting a
 * reactive form that lets the user enter the space name, an optional
 * multilingual title and description, an optional expiration date and a toggle
 * controlling whether the creator wants to manage the new space's permissions.
 *
 * On submission it creates the space through {@link SpaceService}. When the
 * user opts out of the default inherited permissions, it initialises the
 * permission definition through {@link PermissionService} and navigates to the
 * permissions editor.
 *
 * Key collaborators: {@link SpaceService} (fetch siblings / create the space),
 * {@link PermissionService} (permission bootstrap), {@link ActionService}
 * (broadcast the completed action), {@link TranslocoService} (active language)
 * and the Angular {@link Router} / {@link ActivatedRoute} for navigation.
 */
@Component({
  selector: 'cbc-add-space',
  templateUrl: './add-space.component.html',
  styleUrl: './add-space.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    MultilingualInputComponent,
    HintComponent,
    MatSlideToggleModule,
    TranslocoModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
  ],
})
export class AddSpaceComponent implements OnChanges, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly spaceService = inject(SpaceService);
  private readonly translateService = inject(TranslocoService);
  private readonly actionService = inject(ActionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly permissionService = inject(PermissionService);

  /** Reference to the "manage permissions" slide toggle element in the template. */
  readonly permitedToogle = viewChild.required<ElementRef>('permitedToogle');

  /** Backing state for the "manage permissions" toggle. */
  public checked = false;

  /**
   * Required input controlling the visibility of the wizard/modal. When it
   * transitions to `true`, the sibling contents are (re)loaded and the form is
   * (re)built.
   */
  public readonly showWizard = input.required<boolean>();

  /** Required input: the parent node under which the new space is created. */
  public readonly parentNode = input.required<ModelNode>();

  /**
   * Emits an {@link ActionEmitterResult} when the modal is closed, carrying the
   * outcome of the create-space action (succeeded, failed or cancelled).
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Paged list of the parent node's existing children, used for name-uniqueness validation. */
  public contents!: PagedNodes;

  /** Reactive form model backing the create-space wizard. */
  public createSpaceForm = signal<FormGroup | undefined>(undefined);

  /** `true` while a space creation request is in flight; used to disable submission. */
  public creating = signal(false);

  /** Permission definition returned when bootstrapping the new space's permissions. */
  public perms!: PermissionDefinition;

  /** `true` when the expiration date is activated but no date has been provided yet. */
  public dateRequired = signal(false);

  /** Subscription driving the calendar/expiration date handling; cleaned up on destroy. */
  private dateSubscription!: Subscription;

  /**
   * Angular lifecycle hook. Loads the parent node's contents (and rebuilds the
   * form) whenever the {@link showWizard} input becomes `true`.
   *
   * @param changes - The set of changed input properties for this cycle.
   * @returns A promise that resolves once the contents have been loaded.
   */
  public ngOnChanges(changes: SimpleChanges) {
    void this.handleChanges(changes);
  }

  private async handleChanges(changes: SimpleChanges) {
    if (changes.showWizard?.currentValue === true) {
      await this.loadContents();
    }
  }

  /**
   * Fetches the parent node's children (used to validate name uniqueness) and
   * builds the reactive form once the contents are available.
   *
   * @returns A promise that resolves once contents are loaded and the form built.
   */
  private async loadContents() {
    this.contents = await this.spaceService.getChildrenAsync({
      id: this.parentNode().id as string,
      language: this.translateService.getActiveLang(),
      guest: false,
      limit: -1,
      page: 1,
      order: 'modified_DESC',
      folderOnly: false,
      fileOnly: false,
      skipExpiredItems: true,
    });

    if (this.contents !== undefined) {
      this.buildForm();
    }
  }

  /**
   * Constructs the reactive form model for the wizard, wiring up validators
   * (required name, name format, and folder-name uniqueness against the loaded
   * contents), calendar date handling, and a subscription that toggles
   * {@link dateRequired} when the expiration date is activated without a value.
   */
  public buildForm(): void {
    const createSpaceForm = this.fb.group(
      {
        name: [
          '',
          [
            Validators.required,
            nameValidator,
            (control: AbstractControl) =>
              fileFolderExistsValidator(control, this.contents.data),
            // more or less equivalent to:
            // function funcValidator(control: AbstractControl) { fileFolderExistsValidator(control, this.contents) }
            // the executor runs every validator of the array passing the control only,
            // so I encapsulate the control passing in a function to add the additional parameter (this.contents) as I need it
          ],
        ],
        description: [''],
        title: [''],
        managePermited: [true],
        expirationDate: [],
        expirationDateActived: [false],
      },
      {
        updateOn: 'change',
      }
    );
    this.createSpaceForm.set(createSpaceForm);

    this.dateSubscription = setupCalendarDateHandling(
      createSpaceForm.controls.expirationDate
    );

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    createSpaceForm.valueChanges.subscribe((value: any) => {
      if (
        value.expirationDateActived &&
        createSpaceForm.controls.expirationDate.value === null
      ) {
        this.dateRequired.set(true);
      } else {
        this.dateRequired.set(false);
      }
    });
  }

  /**
   * Cancels the wizard: resets the form and emits a {@link ActionEmitterResult}
   * with a {@link ActionResult.CANCELED} result to close the modal.
   *
   * @param _action - The originating action identifier (unused).
   */
  public cancelWizard(_action: string): void {
    this.createSpaceForm()?.reset();

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.CREATE_SPACE;

    this.modalHide.emit(result);
  }

  /**
   * Creates a new sub-space under the current parent node from the form values.
   *
   * When the form is valid, it builds the {@link ModelNode} payload (including
   * an optional expiration date) and posts it via {@link SpaceService}. If the
   * user chose not to manage permissions, the created node and success result
   * are emitted; otherwise permission bootstrapping is delegated to
   * {@link managerPermission}. On failure a {@link ActionResult.FAILED} result
   * is emitted. In all cases the completed action is propagated through
   * {@link ActionService} and the form is reset.
   *
   * @returns A promise that resolves once the create flow has completed.
   */
  public async createSpace() {
    this.creating.set(true);
    const parentNode = this.parentNode();
    const createSpaceForm = this.createSpaceForm();
    if (parentNode.id !== undefined && createSpaceForm !== undefined) {
      if (createSpaceForm.valid) {
        const result: ActionEmitterResult = {};
        result.type = ActionType.CREATE_SPACE;
        try {
          const spaceNode: ModelNode = {
            id: '',
            name: createSpaceForm.value.name,
            description: removeNulls(createSpaceForm.value.description),
            title: removeNulls(createSpaceForm.value.title),
            properties: {
              expiration_date: createSpaceForm.value.expirationDateActived
                ? createSpaceForm.value.expirationDate
                : '',
            },
          };
          const response = await this.spaceService.postSubspaceAsync({
            id: parentNode.id,
            node: spaceNode,
          });

          if (createSpaceForm.value.managePermited) {
            result.node = response;
            result.result = ActionResult.SUCCEED;
          } else {
            this.managerPermission(response);
          }
        } catch (error) {
          console.error(error);
          result.result = ActionResult.FAILED;
        }
        this.modalHide.emit(result);
        this.actionService.propagateActionFinished(result);
      }
    }
    this.creating.set(false);
    createSpaceForm?.reset();
    createSpaceForm?.controls.managePermited.setValue(true);
  }

  /**
   * Initialises a non-inherited permission definition for the newly created
   * space and navigates the user to the permissions editor.
   *
   * @param newFolderNode - The node representing the space just created.
   * @returns A promise that resolves once permissions are set and navigation triggered.
   */
  public async managerPermission(newFolderNode: ModelNode) {
    const body: PermissionDefinition = {
      inherited: false,
      permissions: {
        profiles: [],
        users: [],
      },
    };

    if (this.parentNode()?.id) {
      this.perms = await this.permissionService.putPermissionAsync({
        id: newFolderNode.id as string,
        permissionDefinition: body,
      });
    }

    this.router.navigate(['../../permissions', newFolderNode.id], {
      queryParams: { from: 'library' },
      relativeTo: this.route,
    });
  }

  /**
   * Convenience accessor for the form's `name` control.
   *
   * @returns The {@link AbstractControl} backing the space name field.
   */
  get nameControl(): AbstractControl {
    const form = this.createSpaceForm();
    if (!form) {
      throw new Error('createSpaceForm has not been initialized');
    }
    return form.controls.name;
  }

  /**
   * Determines whether the currently selected expiration date is in the past.
   *
   * @returns `true` if the selected expiration date is before now.
   */
  public isExpired() {
    return this.createSpaceForm()?.value.expirationDate < Date.now();
  }

  /**
   * Angular lifecycle hook. Unsubscribes from the calendar date handling
   * subscription to avoid memory leaks.
   */
  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }
}
