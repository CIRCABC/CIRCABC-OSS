import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AppMessageService } from 'app/core/generated/circabc';
import { setupCalendarDateHandling } from 'app/core/util/date-calendar-util';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { Subscription } from 'rxjs';

/**
 * Standalone Angular component that renders the reactive form used to create
 * or edit an application system-message template.
 *
 * The template displays fields for the message content (rich text), display
 * time, an optional auto-close date, severity level, an "enabled" toggle and
 * a "notification" toggle. When the route contains an `id` parameter the
 * component switches to update mode and pre-fills the form with the existing
 * template fetched from the backend.
 *
 * Key collaborators:
 * - {@link AppMessageService} (generated CIRCABC API client) to load, create
 *   and update message templates.
 * - {@link FormBuilder} to build the reactive form.
 * - {@link Router} / {@link ActivatedRoute} for navigation and reading the
 *   template id from the URL.
 * - `setupCalendarDateHandling` to wire up calendar/date handling for the
 *   auto-close date control.
 */
@Component({
  selector: 'cbc-create-template',
  templateUrl: './create-template.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RichTextEditorComponent,
    ControlMessageComponent,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class CreateTemplateComponent implements OnInit, OnDestroy {
  /** Factory used to build the reactive template form. */
  private readonly fb = inject(FormBuilder);
  /** Generated API client used to load, create and update message templates. */
  private readonly appMessageService = inject(AppMessageService);
  /** Router used to navigate back to the system-message list after save/cancel. */
  private readonly router = inject(Router);
  /** Provides access to the current route parameters (notably the template id). */
  private readonly activatedRoute = inject(ActivatedRoute);
  /**
   * Injected to mark the view dirty when the reactive form changes through the
   * Quill-based rich text editor: it propagates content via Quill's own
   * `text-change` callback, which is outside Angular's event system and does
   * not mark this OnPush component for check on its own.
   */
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  /** Reactive form holding the template fields; initialized in {@link ngOnInit}. */
  public templateForm!: FormGroup;
  /** Whether a save/update operation is currently in progress. */
  public readonly processing = signal(false);
  /** True when editing an existing template, false when creating a new one. */
  public readonly updateMode = signal(false);
  /** Subscription for the auto-close date handling; cleaned up on destroy. */
  private dateSubscription!: Subscription;
  /** Subscription marking the view for check on form changes; cleaned up on destroy. */
  private formSubscription!: Subscription;

  /**
   * Angular lifecycle hook. Builds the reactive form, wires up calendar date
   * handling for the closure date control, and, when the route contains an
   * `id` parameter, loads the matching template into the form.
   */
  ngOnInit() {
    this.templateForm = this.fb.group({
      id: [''],
      content: ['', Validators.required],
      displayTime: [15],
      dateClosure: [''],
      level: ['info'],
      enabled: [false],
      notification: [false],
    });

    this.dateSubscription = setupCalendarDateHandling(
      this.templateForm.controls.dateClosure
    );

    // The rich text editor updates `content` through Quill's `text-change`
    // callback (a third-party event outside Angular), so mark the view for
    // check on form changes to keep template reads such as `isFormValid()`
    // in sync under OnPush.
    this.formSubscription = this.templateForm.valueChanges.subscribe(() => {
      this.changeDetectorRef.markForCheck();
    });

    if (this.activatedRoute.params) {
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.id) {
          await this.getTemplate(params.id);
        }
      });
    }
  }

  /**
   * Angular lifecycle hook. Unsubscribes from the date-handling subscription
   * to avoid memory leaks.
   */
  ngOnDestroy() {
    if (this.dateSubscription) {
      this.dateSubscription.unsubscribe();
    }
    if (this.formSubscription) {
      this.formSubscription.unsubscribe();
    }
  }

  /**
   * Loads an existing template from the backend and patches its values into
   * the form, switching the component into update mode.
   *
   * @param templateID Identifier of the template to load.
   * @returns A promise that resolves once the form has been populated.
   */
  private async getTemplate(templateID: string) {
    this.updateMode.set(true);
    const data = await this.appMessageService.getAppMessageTemplateAsync({
      id: templateID,
    });
    let autoClose: string | Date = '';
    if (
      data.dateClosure !== undefined &&
      data.dateClosure.toLocaleString().length > 0
    ) {
      autoClose = new Date(data.dateClosure);
    }
    this.templateForm.patchValue({
      id: data.id,
      content: data.content,
      displayTime: data.displayTime,
      dateClosure: autoClose,
      level: data.level,
      enabled: data.enabled,
    });
  }

  /**
   * Indicates whether the template is currently marked as enabled.
   *
   * @returns `true` if the form's `enabled` value is truthy, otherwise `false`.
   */
  public isEnabled(): boolean {
    if (this.templateForm?.value) {
      return this.templateForm.value.enabled;
    }
    return false;
  }

  /** The form control backing the rich-text message content field. */
  get contentControl(): AbstractControl {
    return this.templateForm.controls.content;
  }

  /**
   * Persists the current form as a template. Creates a new template or updates
   * the existing one depending on {@link updateMode}, then navigates back to
   * the system-message list. Errors are logged to the console.
   *
   * @returns A promise that resolves once the save/update flow completes.
   */
  public async saveOrUpdateTemplate() {
    this.processing.set(true);

    try {
      if (this.updateMode()) {
        await this.appMessageService.updateAppMessageTemplateAsync({
          id: this.templateForm.value.id,
          appMessage: this.templateForm.value,
          notification: this.templateForm.value.notification,
        });
      } else {
        await this.appMessageService.addAppMessageTemplateAsync({
          appMessage: this.templateForm.value,
          notification: this.templateForm.value.notification,
        });
      }

      this.router.navigate(['support', 'system-message']);
    } catch (error) {
      console.error(error);
    }

    this.processing.set(false);
  }

  /**
   * Indicates whether the template form currently passes validation.
   *
   * @returns `true` if the form exists and is valid, otherwise `false`.
   */
  public isFormValid(): boolean {
    if (this.templateForm) {
      return this.templateForm.valid;
    }

    return false;
  }

  /** Clears the auto-close date value from the form. */
  public cleanClosure() {
    if (this.templateForm) {
      this.templateForm.patchValue({ dateClosure: '' });
    }
  }

  /**
   * Indicates whether the notification option is currently selected.
   *
   * @returns `true` if the form's `notification` value is truthy, otherwise `false`.
   */
  public isNotified(): boolean {
    if (this.templateForm) {
      return this.templateForm.value.notification;
    }

    return false;
  }

  /**
   * Cancels the edit/create flow and navigates back to the system-message list.
   *
   * @returns A promise that resolves once navigation is triggered.
   */
  public async cancel() {
    this.router.navigate(['support', 'system-message']);
  }

  /**
   * Keeps the notification flag consistent with the enabled state: when the
   * template is not enabled, the notification option is forced off.
   */
  public checkNotification() {
    if (this.templateForm && !this.templateForm.value.enabled) {
      this.templateForm.controls.notification.setValue(false);
    }
  }
}
