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

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  ContentService,
  Node as ModelNode,
  MultilingualAspectMetadata,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { ModalComponent } from 'app/shared/modal/modal.component';

/**
 * Modal component that lets a user enable the multilingual aspect on a
 * library content node.
 *
 * It renders a modal dialog (via {@link ModalComponent}) containing a
 * reactive form where the user selects a pivot language and provides an
 * author. On submission it calls the CIRCABC {@link ContentService} to post
 * the multilingual aspect metadata to the backend and reports the outcome to
 * the parent through its outputs.
 *
 * Key collaborators:
 * - {@link ContentService} — performs the backend call to enable the aspect.
 * - {@link TranslocoService} — supplies the default language and error
 *   message translations.
 * - {@link UiMessageService} — surfaces error notifications to the user.
 */
@Component({
  selector: 'cbc-enable-multilingual',
  templateUrl: './enable-multilingual.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    ControlMessageComponent,
    LangSelectorComponent,
    TranslocoModule,
  ],
})
export class EnableMultilingualComponent implements OnInit {
  /** Service used to display error notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Transloco service providing the default language and translations. */
  private readonly translateService = inject(TranslocoService);
  /** Factory used to build the reactive form. */
  private readonly fb = inject(FormBuilder);
  /** CIRCABC content API client used to enable the multilingual aspect. */
  private readonly contentService = inject(ContentService);

  /** Whether the modal dialog is currently visible. */
  readonly showModal = input(false);
  /** Required node on which the multilingual aspect should be enabled. */
  readonly targetNode = input.required<ModelNode>();
  /** Emitted when the user cancels the operation. */
  readonly modalCanceled = output<ActionEmitterResult>();
  /** Emitted after an attempt to enable the multilingual aspect completes. */
  readonly mutlilingualEnabled = output<ActionEmitterResult>();

  /** Reactive form holding the `author` and pivot `lang` fields. */
  public enableMultilingualForm!: FormGroup;
  /** Whether a backend request is currently in progress. */
  public readonly processing = signal(false);

  /**
   * Initializes the reactive form with an empty required `author` field and a
   * required `lang` field defaulting to the application's default language.
   */
  ngOnInit() {
    this.enableMultilingualForm = this.fb.group(
      {
        author: ['', Validators.required],
        lang: [this.translateService.getDefaultLang(), Validators.required],
      },
      {
        updateOn: 'change',
      }
    );
  }

  /**
   * Enables the multilingual aspect on the current target node.
   *
   * Builds the {@link MultilingualAspectMetadata} from the form values
   * (pivot language and author) and posts it via {@link ContentService}.
   * On success or failure it emits an {@link ActionEmitterResult} of type
   * `ENABLE_MULTILINGUAL` through the `mutlilingualEnabled` output; on
   * failure it also displays an error message. Does nothing if the target
   * node has no id.
   *
   * @returns A promise that resolves once the request has completed and the
   * result has been emitted.
   */
  async enableMultilingual() {
    const targetNode = this.targetNode();
    if (targetNode.id) {
      this.processing.set(true);
      const data: MultilingualAspectMetadata = {};
      data.pivotLang = this.enableMultilingualForm.value.lang;
      data.author = this.enableMultilingualForm.value.author;

      const result: ActionEmitterResult = {};
      result.type = ActionType.ENABLE_MULTILINGUAL;

      try {
        await this.contentService.postMultilingualAspectAsync({
          id: targetNode.id,
          multilingualAspectMetadata: data,
        });
        result.result = ActionResult.SUCCEED;
      } catch (error) {
        console.error(error);
        result.result = ActionResult.FAILED;
        const txt = this.translateService.translate(
          getErrorTranslation(ActionType.ENABLE_MULTILINGUAL)
        );
        this.uiMessageService.addErrorMessage(txt, false);
      }

      this.mutlilingualEnabled.emit(result);

      this.processing.set(false);
    }
  }

  /**
   * Cancels the operation, resets the processing state and emits a canceled
   * {@link ActionEmitterResult} of type `ENABLE_MULTILINGUAL` through the
   * `modalCanceled` output.
   */
  cancel() {
    this.processing.set(false);

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.ENABLE_MULTILINGUAL;
    this.modalCanceled.emit(result);
  }

  /**
   * Convenience accessor for the form's `author` control, used by the
   * template to display validation messages.
   *
   * @returns The `author` {@link AbstractControl} of the form.
   */
  get authorControl(): AbstractControl {
    return this.enableMultilingualForm.controls.author;
  }
}
