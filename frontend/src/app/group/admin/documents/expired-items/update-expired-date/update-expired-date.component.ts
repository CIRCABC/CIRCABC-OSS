import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
  OnChanges,
  OnDestroy,
  OnInit,
  output,
  SimpleChanges,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  ContentService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { Subscription } from 'rxjs';

/**
 * Modal component used within the group administration "expired items" area to
 * view and update the expiration date of a selected library node (file or
 * folder/space).
 *
 * It renders a modal dialog containing a reactive form with a slide toggle to
 * activate/deactivate the expiration date, a date picker to choose the
 * expiration date, and quick-select default values. On save it persists the
 * change through {@link ContentService} for files or {@link SpaceService} for
 * folders/spaces, then closes the modal and notifies parent components of the
 * outcome.
 *
 * Key collaborators:
 * - {@link FormBuilder} to build the reactive form.
 * - {@link ContentService} to update file nodes.
 * - {@link SpaceService} to update folder/space nodes.
 * - {@link setupCalendarDateHandling} to wire up date picker handling.
 */
@Component({
  selector: 'cbc-update-expired-date',
  templateUrl: './update-expired-date.component.html',
  styleUrl: './update-expired-date.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    MatSlideToggleModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    TranslocoModule,
  ],
})
export class UpdateExpiredDateComponent
  implements OnInit, OnChanges, OnDestroy
{
  /** Reactive form builder used to construct the expiration date form. */
  private readonly fb = inject(FormBuilder);
  /** API client used to persist expiration changes for file nodes. */
  private readonly contentService = inject(ContentService);
  /** API client used to persist expiration changes for folder/space nodes. */
  private readonly spaceService = inject(SpaceService);

  /**
   * Required input controlling the modal visibility, aliased as `showModal`
   * from the parent template. Backs the writable {@link showModal} signal.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  public showModalInput = input.required<boolean>({ alias: 'showModal' });
  /**
   * Writable signal derived from {@link showModalInput} that tracks whether the
   * modal is currently displayed. It can be updated internally (e.g. on save or
   * cancel) while staying linked to the input value.
   */
  public showModal = linkedSignal(this.showModalInput);

  /** Required input: the library node (file or folder/space) being edited. */
  public readonly nodeSelected = input.required<ModelNode>();
  /** Output emitted when the modal visibility changes, to sync the parent's two-way binding. */
  public readonly showModalChange = output();
  /** Output emitted when the modal is hidden after a save, carrying the action outcome. */
  public readonly modalHide = output<ActionEmitterResult>();

  /** Reactive form holding the node name, expiration date and its activation toggle. */
  public editExpiedDateNodeForm!: FormGroup;
  /** Quick-select default durations (in days) offered to the user. */
  public defaultValues = [7, 15, 30];
  /** Whether a save operation is currently in progress. */
  public processing = false;
  /** Subscription for the date picker handling wiring, cleaned up on destroy. */
  private dateSubscription!: Subscription;

  /**
   * Angular lifecycle hook. Builds the reactive form and sets up date picker
   * calendar handling for the expiration date control.
   */
  public ngOnInit() {
    this.editExpiedDateNodeForm = this.fb.group(
      {
        name: [''],
        expirationDate: [],
        expirationDateActived: [],
      },
      {
        updateOn: 'change',
      }
    );

    this.dateSubscription = setupCalendarDateHandling(
      this.editExpiedDateNodeForm.controls.expirationDate
    );
  }

  /**
   * Angular lifecycle hook. Unsubscribes from the date handling subscription to
   * prevent memory leaks.
   */
  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
  }

  /**
   * Angular lifecycle hook. Reloads the form whenever the selected node input
   * changes.
   *
   * @param changes - The set of changed input properties provided by Angular.
   */
  ngOnChanges(changes: SimpleChanges) {
    void this.handleChanges(changes);
  }

  private async handleChanges(changes: SimpleChanges) {
    if (changes.nodeSelected) {
      this.loadForm(changes.nodeSelected.currentValue);
    }
  }

  /**
   * Populates the form controls from the given node, setting the name, the
   * expiration date and whether the expiration date is currently active based
   * on the node's `expiration_date` property.
   *
   * @param node - The node whose properties are used to initialise the form.
   */
  loadForm(node: ModelNode): void {
    if (this.editExpiedDateNodeForm && this.nodeSelected() && node.properties) {
      this.editExpiedDateNodeForm.controls.name.setValue(node.name);

      this.editExpiedDateNodeForm.controls.expirationDate.setValue(
        node.properties.expiration_date ? new Date() : undefined
      );

      if (
        node.properties.expiration_date === '' ||
        node.properties.expiration_date === undefined
      ) {
        this.editExpiedDateNodeForm.controls.expirationDateActived.setValue(
          false
        );
      } else {
        this.editExpiedDateNodeForm.controls.expirationDateActived.setValue(
          true
        );
      }
    }
  }

  /**
   * Builds an updated node from the current form values and persists the
   * expiration date change. Files are updated via {@link ContentService} and
   * folders/spaces via {@link SpaceService}. When the expiration date is not
   * activated or is empty, the expiration date is cleared. On success, hides
   * the modal and emits {@link showModalChange} and {@link modalHide} with a
   * succeeded {@link ActionResult}.
   *
   * @returns A promise that resolves once the update request completes and the
   * modal has been closed.
   */
  public async saveConfiguration() {
    const tmpNode: ModelNode = {
      id: this.nodeSelected().id,
      name: this.nodeSelected().name,
      title: this.nodeSelected().title,
      description: this.nodeSelected().description,
      properties: {},
    };

    const isFile = this.isFile();

    const nodeSelected = this.nodeSelected();
    if (!isFile) {
      tmpNode.properties = {
        expiration_date:
          this.editExpiedDateNodeForm.value.expirationDate === undefined ||
          this.editExpiedDateNodeForm.value.expirationDate === '' ||
          !this.editExpiedDateNodeForm.value.expirationDateActived
            ? null
            : this.editExpiedDateNodeForm.value.expirationDate,
      };
    } else if (nodeSelected.properties) {
      tmpNode.properties = {
        expiration_date:
          this.editExpiedDateNodeForm.value.expirationDate === undefined ||
          this.editExpiedDateNodeForm.value.expirationDate === '' ||
          !this.editExpiedDateNodeForm.value.expirationDateActived
            ? 'null'
            : this.editExpiedDateNodeForm.value.expirationDate,
        issue_date: nodeSelected.properties.issueDate,
        encoding: nodeSelected.properties.encoding,
        mimetype: nodeSelected.properties.mimetype,
        reference: nodeSelected.properties.reference,
        author: nodeSelected.properties.author,
        url: nodeSelected.properties.url,
        status: nodeSelected.properties.status,
        security: nodeSelected.properties.security_ranking,
      };
    }

    if (isFile) {
      await this.contentService.putContentAsync({
        id: nodeSelected.id as string,
        node: tmpNode,
      });
    } else {
      await this.spaceService.putSpaceAsync({
        id: nodeSelected.id as string,
        node: tmpNode,
      });
    }

    this.showModal.set(false);
    this.showModalChange.emit();
    this.modalHide.emit({ result: ActionResult.SUCCEED });
  }

  /**
   * Closes the modal without saving and notifies the parent via
   * {@link showModalChange}.
   */
  public cancel() {
    this.showModal.set(false);
    this.showModalChange.emit();
  }

  /**
   * Determines whether the currently selected expiration date is in the past.
   *
   * @returns `true` if the form's expiration date is earlier than the current
   * time, otherwise `false`.
   */
  public isExpired() {
    return this.editExpiedDateNodeForm.value.expirationDate < Date.now();
  }

  /**
   * Determines whether the selected node is a file (as opposed to a
   * folder/space) based on its `type`.
   *
   * @returns `true` if the node type does not include `folder`, otherwise
   * `false`.
   */
  public isFile(): boolean {
    let result = false;
    const nodeSelected = this.nodeSelected();
    if (nodeSelected?.type !== undefined) {
      result = !nodeSelected.type.includes('folder');
    }
    return result;
  }

  /**
   * Provides the current date, used as the minimum selectable value for the
   * date picker.
   *
   * @returns A {@link Date} representing the current date and time.
   */
  public getCurrentDate() {
    return new Date();
  }
}
