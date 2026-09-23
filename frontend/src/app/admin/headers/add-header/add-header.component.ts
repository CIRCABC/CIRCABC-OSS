import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
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
import { Header, HeaderService } from 'app/core/generated/circabc';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Modal component that renders a form for creating a new CIRCABC header.
 *
 * The component displays a modal dialog (via {@link ModalComponent}) containing
 * a reactive form with a multilingual name field and a description field. On
 * submission it delegates persistence to the {@link HeaderService}. It performs
 * client-side validation to reject names that collide with existing headers.
 *
 * Key collaborators:
 * - {@link HeaderService} — REST client used to persist the new header.
 * - {@link FormBuilder} — builds the reactive form group.
 */
@Component({
  selector: 'cbc-add-header',
  templateUrl: './add-header.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    DataCyDirective,
    ControlMessageComponent,
    MultilingualInputComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AddHeaderComponent implements OnInit {
  /** REST client used to create the header on the backend. */
  private readonly headerService = inject(HeaderService);
  /** Factory used to construct the reactive {@link headerForm}. */
  private readonly formBuilder = inject(FormBuilder);

  /**
   * Input controlling the modal's visibility, aliased as `showModal`.
   * Backs the writable {@link showModal} signal.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  public showModalInput = input<boolean>(false, { alias: 'showModal' });
  /**
   * Writable signal mirroring the {@link showModalInput} value, allowing the
   * component to open/close the modal locally (e.g. on cancel).
   */
  public showModal = linkedSignal(this.showModalInput);
  /**
   * Output emitted after a header has been successfully created, signalling the
   * parent to refresh and/or close the modal.
   */
  public readonly showModalChange = output();
  /**
   * Input list of existing headers, used by the validator to prevent creating a
   * header with a name that is already taken.
   */
  public readonly headers = input<Header[]>();
  /** Whether a create request is currently in flight (drives the spinner/disabled state). */
  public readonly processing = signal(false);
  /** Reactive form holding the `name` and `description` controls. */
  public headerForm!: FormGroup;

  /**
   * Angular lifecycle hook that initializes {@link headerForm} with the `name`
   * (required and validated against existing header names) and `description`
   * (required) controls.
   */
  ngOnInit() {
    this.headerForm = this.formBuilder.group(
      {
        // the fat arrow function captures "this" from the surrounding context and passes it on
        name: [
          '',
          [
            Validators.required,
            (control: AbstractControl) =>
              this.forbiddenNameArrayValidator(control),
          ],
        ],
        description: ['', [Validators.required]],
      },
      {
        updateOn: 'change',
      }
    );
  }

  // validates that the given name does exist
  /**
   * Validator that rejects a name already used by an existing header.
   *
   * @param control - The form control whose value is the candidate header name.
   * @returns `{ forbiddenNameArray: { name } }` if the name collides with an
   *   existing header, otherwise `null` (including when no headers are provided).
   */
  public forbiddenNameArrayValidator(
    control: AbstractControl
  ): { [key: string]: {} } | null {
    const headers = this.headers();
    if (!headers) {
      return null;
    }
    const name = control.value;

    const headerNames = headers.map((header: Header) => header.name);
    const no = headerNames.includes(name);
    return no ? { forbiddenNameArray: { name } } : null;
  }

  /** Closes the modal without creating a header. */
  public cancel() {
    this.showModal.set(false);
  }

  /**
   * Creates a new header from the current form values.
   *
   * Does nothing if the form is invalid. While the request is in flight
   * {@link processing} is set to `true`. On success {@link showModalChange} is
   * emitted to notify the parent.
   *
   * @returns A promise that resolves once the create request completes (whether
   *   it succeeds or fails).
   */
  public async addHeader() {
    if (this.headerForm.status !== 'VALID') {
      return;
    }

    try {
      this.processing.set(true);

      const header: Header = { name: '' };

      header.name = this.headerForm.controls.name.value;
      header.description = this.headerForm.controls.description.value;

      await this.headerService.postHeaderAsync({ header });
      this.showModalChange.emit();
    } finally {
      this.processing.set(false);
    }
  }

  /** Convenience accessor for the `name` form control. */
  get nameControl(): AbstractControl {
    return this.headerForm.controls.name;
  }

  /** Convenience accessor for the `description` form control. */
  get descriptionControl(): AbstractControl {
    return this.headerForm.controls.description;
  }
}
