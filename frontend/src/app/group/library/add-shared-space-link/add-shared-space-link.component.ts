import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  Node as ModelNode,
  ShareSpaceItem,
  SpaceService,
} from 'app/core/generated/circabc';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that renders a reactive form for creating a link to a
 * shared space inside the document library.
 *
 * The component displays a {@link ModalComponent} containing a form with a
 * title, an optional description and a selectable shared space id. On
 * submission it delegates to {@link SpaceService} to export the selected
 * shared space under the current parent node, then emits the outcome so the
 * host can close the modal and react to success, failure or cancellation.
 *
 * Key collaborators:
 * - {@link FormBuilder} to build the reactive form group.
 * - {@link SpaceService} (generated CIRCABC API client) to persist the link.
 */
@Component({
  selector: 'cbc-add-shared-space-link',
  templateUrl: './add-shared-space-link.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    TranslocoModule,
  ],
})
export class AddSharedSpaceLinkComponent implements OnInit {
  /** Builder used to construct the reactive form group. */
  private readonly fb = inject(FormBuilder);
  /** Generated CIRCABC API client used to persist the shared space link. */
  private readonly spaceService = inject(SpaceService);

  /**
   * Input controlling the visibility of the underlying modal dialog.
   * When `true` the modal is shown; defaults to `false`.
   */
  readonly showModal = input(false);
  /**
   * Required input holding the node under which the shared space link is
   * created. Its `id` is used as the destination parent for the export.
   */
  public readonly parentNode = input.required<ModelNode>();
  /**
   * Required input listing the shared spaces available for selection in the
   * form's shared space dropdown.
   */
  public readonly sharedSpaceItems = input.required<ShareSpaceItem[]>();
  /**
   * Output emitted when the modal should be dismissed, carrying the outcome
   * of the operation (succeeded, failed or canceled).
   */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Reactive form group holding the title, description and shared space id. */
  public createSharedSpaceLinkForm!: FormGroup;
  /** Flag indicating whether a create request is currently in progress. */
  public readonly processing = signal(false);

  /**
   * Angular lifecycle hook that initializes the reactive form group with the
   * title (required), description (optional) and shared space id (required)
   * controls.
   */
  ngOnInit() {
    this.createSharedSpaceLinkForm = this.fb.group(
      {
        title: ['', Validators.required],
        description: [''],
        sharedSpaceId: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Submits the form to create a shared space link under the parent node.
   *
   * Sets {@link processing} while the request is in flight, calls
   * {@link SpaceService.postExportedSharedSpace} with the selected shared
   * space id and the form values, and emits an {@link ActionEmitterResult}
   * via {@link modalHide} indicating whether the operation succeeded or
   * failed. On success the form is reset. No request is made when the parent
   * node has no `id`.
   *
   * @returns A promise that resolves once the request completes and the
   * result has been emitted.
   */
  async createSharedSpaceLink() {
    this.processing.set(true);

    const parentNode = this.parentNode();
    if (parentNode.id !== undefined) {
      const res: ActionEmitterResult = {};
      res.type = ActionType.ADD_SHARED_SPACE_LINK;

      try {
        await this.spaceService.postExportedSharedSpaceAsync({
          id: this.createSharedSpaceLinkForm.value.sharedSpaceId,
          parentId: parentNode.id,
          title: this.createSharedSpaceLinkForm.value.title,
          description: this.createSharedSpaceLinkForm.value.description,
        });
        res.result = ActionResult.SUCCEED;
        this.createSharedSpaceLinkForm.reset();
      } catch (error) {
        console.error(error);
        res.result = ActionResult.FAILED;
      }

      this.modalHide.emit(res);
    }

    this.processing.set(false);
  }

  /**
   * Cancels the operation, resets the form and emits an
   * {@link ActionEmitterResult} with a canceled result via {@link modalHide}
   * so the host can close the modal.
   */
  cancel() {
    this.createSharedSpaceLinkForm.reset();
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.ADD_SHARED_SPACE_LINK;
    this.modalHide.emit(res);
  }

  /**
   * Accessor for the form's title control.
   * @returns The `title` form control.
   */
  get titleControl(): AbstractControl {
    return this.createSharedSpaceLinkForm.controls.title;
  }

  /**
   * Accessor for the form's description control.
   * @returns The `description` form control.
   */
  get descriptionControl(): AbstractControl {
    return this.createSharedSpaceLinkForm.controls.description;
  }

  /**
   * Accessor for the form's shared space id control.
   * @returns The `sharedSpaceId` form control.
   */
  get sharedSpaceIdControl(): AbstractControl {
    return this.createSharedSpaceLinkForm.controls.sharedSpaceId;
  }
}
