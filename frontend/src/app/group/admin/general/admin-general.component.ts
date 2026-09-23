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
import { ActivatedRoute } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { GroupReloadListenerService } from 'app/core/group-reload-listener.service';
import { fileNameValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { FocusDirective } from 'app/shared/directives/focus.directive';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component that renders the "General" administration
 * screen for an interest group.
 *
 * The template presents a reactive form allowing group administrators to view
 * and edit the group's core metadata (identifier, name, multilingual title and
 * description, and contact information). It loads the currently routed
 * interest group, lets the user save changes back to the backend, and cancel
 * pending edits by reloading the persisted values. A spinner is shown while a
 * save operation is in progress.
 *
 * Key collaborators:
 * - {@link InterestGroupService} to read and update the interest group.
 * - {@link GroupReloadListenerService} to notify other parts of the app that
 *   the group has changed after a successful save.
 * - {@link ActivatedRoute} to obtain the routed group `id`.
 * - {@link FormBuilder} to construct the reactive form.
 */
@Component({
  selector: 'cbc-admin-general',
  templateUrl: './admin-general.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    FocusDirective,
    ControlMessageComponent,
    MultilingualInputComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class AdminGeneralComponent implements OnInit {
  /** Route accessor used to read the interest group `id` route parameter. */
  private readonly route = inject(ActivatedRoute);
  /** API client used to load and persist the interest group. */
  private readonly groupsService = inject(InterestGroupService);
  /** Reactive forms builder used to construct {@link igForm}. */
  private readonly fb = inject(FormBuilder);
  /** Service used to broadcast a refresh once the group has been updated. */
  private readonly groupReloadListenerService = inject(
    GroupReloadListenerService
  );

  /** The interest group currently being edited, loaded from the backend. */
  public ig!: InterestGroup;
  /** Reactive form holding the editable general metadata of the group. */
  public igForm!: FormGroup;
  /** Whether a save operation is currently in progress (drives the spinner). */
  public saving = signal(false);

  /**
   * Angular lifecycle hook.
   *
   * Builds the reactive form (with the `name` field required and validated by
   * {@link fileNameValidator}) and subscribes to route parameter changes so
   * that the corresponding interest group is loaded whenever the route `id`
   * changes.
   */
  ngOnInit() {
    this.igForm = this.fb.group(
      {
        id: [''],
        name: ['', [Validators.required, fileNameValidator]],
        title: [''],
        description: [''],
        contact: [''],
      },
      {
        updateOn: 'change',
      }
    );

    this.route.params.subscribe(async (params) => await this.loadIg(params));
  }

  /**
   * Loads the interest group identified by the given route parameters and
   * populates {@link igForm} with its values.
   *
   * If no `id` is present the form is left untouched. After loading, the window
   * is scrolled back to the top.
   *
   * @param params Route parameters; the `id` entry selects the group to load.
   * @returns A promise that resolves once the group has been loaded (or
   *   immediately if no `id` was provided).
   */
  public async loadIg(params: { [key: string]: string }) {
    const id = params.id;

    if (id) {
      this.ig = await this.groupsService.getInterestGroupAsync({ id });

      this.igForm.patchValue({ id: this.ig.id });
      this.igForm.patchValue({ name: this.ig.name });
      this.igForm.patchValue({ title: this.ig.title });
      this.igForm.patchValue({ description: this.ig.description });
      this.igForm.patchValue({ contact: this.ig.contact });
    }

    // scroll top not needed in angular 9 but needed in angular 10
    window.scroll(0, 0);
  }

  /**
   * Discards any unsaved edits by reloading the current group's persisted
   * values into the form.
   *
   * @returns A promise that resolves once the group has been reloaded.
   */
  public async cancel() {
    await this.loadIg({ id: this.ig.id as string });
  }

  /**
   * Persists the current form values for the interest group.
   *
   * Does nothing if the form is invalid. On success the group is reloaded and a
   * group refresh is broadcast via {@link GroupReloadListenerService}. Any error
   * raised while saving is logged to the console; the {@link saving} flag is
   * always cleared when the operation completes.
   *
   * @returns A promise that resolves once the save attempt has finished.
   */
  public async save() {
    if (this.igForm.valid) {
      this.saving.set(true);
      const body = this.igForm.value;
      try {
        await this.groupsService.putInterestGroupAsync({
          id: this.ig.id as string,
          interestGroup: body,
        });
        await this.loadIg({ id: this.ig.id as string });
        if (this.ig.id) {
          this.groupReloadListenerService.propagateGroupRefresh(this.ig.id);
        }
      } catch (error) {
        console.error(error);
      } finally {
        this.saving.set(false);
      }
    }
  }

  /**
   * Convenience accessor for the form's `name` control, primarily used by the
   * template to display validation messages.
   *
   * @returns The `name` {@link AbstractControl} of {@link igForm}.
   */
  get nameControl(): AbstractControl {
    return this.igForm.controls.name;
  }
}
