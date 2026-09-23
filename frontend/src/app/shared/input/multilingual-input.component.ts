/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  ChangeDetectionStrategy,
  Component,
  forwardRef,
  inject,
  input,
  model,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormBuilder,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { nonEmptyTitle } from 'app/core/validation.service';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import {
  LanguageCodeName,
  supportedLanguages,
} from 'app/shared/langs/supported-langs';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';

/**
 * Form control component that lets a user enter a single logical value
 * (such as a title or description) in multiple languages at once.
 *
 * The component renders a language selector together with an input field
 * (a plain text input, a textarea or a rich text editor depending on
 * configuration) and keeps one value per supported EU language. It
 * implements {@link ControlValueAccessor} so it can be used seamlessly with
 * Angular reactive/template-driven forms via `formControlName`, `formControl`
 * or `ngModel`.
 *
 * The value exchanged with the parent form is a map keyed by language code
 * (e.g. `{ en: 'Hello', fr: 'Bonjour' }`).
 *
 * @see supportedLanguages for the list of languages that are exposed.
 * @see RichTextEditorComponent for the rich-text editing mode.
 */
@Component({
  selector: 'cbc-multilingual-input',
  templateUrl: './multilingual-input.component.html',
  styleUrl: './multilingual-input.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,

      useExisting: forwardRef(() => MultilingualInputComponent),
    },
  ],
  preserveWhitespaces: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    DataCyDirective,
    RichTextEditorComponent,
    TranslocoModule,
  ],
})
export class MultilingualInputComponent
  implements OnInit, ControlValueAccessor, OnChanges
{
  /** Angular `FormBuilder` used to construct the language selector form. */
  private readonly fb = inject(FormBuilder);

  /**
   * Reactive form holding one control per supported language. Each control's
   * value is the translation entered for the corresponding language code.
   */
  form!: FormGroup;
  /**
   * Auxiliary reactive form driving the UI: it holds the currently selected
   * `language` and the `text` bound to the active input field.
   */
  formSelector!: FormGroup;

  /** @Input Initial value for the control (single-language convenience input). */
  readonly value = input<string>();
  /** @Input Placeholder text displayed in the input field when empty. */
  readonly placeholder = input<string>();

  /**
   * Two-way bound model holding the currently selected language code
   * (lower-cased). Defaults to `'en'`.
   */
  lang = model<string>('en');
  /** @Input Required label displayed for the input field. */
  readonly label = input.required<string>();
  /** @Input When `true`, renders a textarea/rich text editor instead of a single-line input. */
  readonly textarea = input(false);
  /** @Input Enables or disables the whole control. Changes are handled in {@link ngOnChanges}. */
  readonly enabled = input(true);

  /**
   * Two-way bound flag controlling the visibility of the translation panel
   * that exposes the additional language fields.
   */
  showTranslationPanel = model<boolean>(false);
  /** @Input When `true`, the value must not be empty (applies the {@link nonEmptyTitle} validator). */
  readonly required = input(false);

  /** Fallback language code used when no explicit language is selected. */
  public defaultLang = 'en';
  /** List of languages the user can enter a translation for. */
  public availableLangs!: LanguageCodeName[];
  /** UI flag indicating whether every language field is selected/displayed. */
  public selectAll = false;

  /**
   * Per-language state map keyed by language code. Each entry tracks whether
   * the language field is currently displayed (`display`) and the translation
   * text entered for that language (`value`).
   */
  public model: { [key: string]: { display: boolean; value: string } } = {};

  // impement ControlValueAccessor interface

  /**
   * Callback registered by the forms API and invoked whenever the value
   * changes. Set via {@link registerOnChange}.
   */
  onChange = (_: any) => {};

  /**
   * Callback registered by the forms API and invoked when the control is
   * touched. Set via {@link registerOnTouched}.
   */
  onTouched = () => {};

  /**
   * `ControlValueAccessor` hook that writes an external value into the
   * component. Populates the per-language {@link model} and patches the
   * underlying form controls, marking languages that have a non-empty value
   * as displayed and syncing the active language field.
   *
   * @param values Map of language code to translation string, or `null`.
   */
  writeValue(values: any) {
    this.showTranslationPanel.set(true);
    if (this.model && this.form) {
      for (const lang of this.availableLangs) {
        const mustDisplay = !!values?.[lang.code];
        const val = values?.[lang.code] ?? '';
        this.model[lang.code] = { display: mustDisplay, value: val };
        this.form.controls[lang.code].patchValue(val);
      }

      if (this.formSelector) {
        this.formSelector.controls.text.patchValue(
          this.model[this.lang()].value
        );
      }
    }
  }

  /**
   * `ControlValueAccessor` hook registering the change callback.
   * @param fn Callback invoked with the new value map when the form changes.
   */
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  /**
   * `ControlValueAccessor` hook registering the touched callback.
   * @param fn Callback invoked when the control is marked as touched.
   */
  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  /** Disables the language form and the active text field. */
  public disableForm() {
    this.form.disable();
    this.formSelector.controls.text.disable();
  }

  /** Enables the language form and the active text field. */
  public enableForm() {
    this.form.enable();
    this.formSelector.controls.text.enable();
  }

  /**
   * Reacts to `@Input` changes. When the `enabled` input changes, enables or
   * disables the form accordingly (only once the form has been initialised).
   *
   * @param changes The set of changed input properties provided by Angular.
   */
  ngOnChanges(changes: SimpleChanges) {
    if (changes.enabled && this.form) {
      if (changes.enabled.currentValue) {
        this.enableForm();
      } else {
        this.disableForm();
      }
    }
  }

  /** Angular lifecycle hook; builds the forms via {@link initForm}. */
  ngOnInit() {
    this.initForm();
  }

  /**
   * Builds the per-language {@link form} and the {@link formSelector} form,
   * wires up validators, initialises the per-language {@link model}, and
   * subscribes to value changes to keep the model, the exposed value and the
   * active language field in sync.
   */
  private initForm() {
    this.lang.set(this.lang().toLowerCase());
    this.availableLangs = supportedLanguages;

    this.form = new FormGroup({});

    if (this.required()) {
      this.form.setValidators([nonEmptyTitle]);
    }

    for (const lang of this.availableLangs) {
      this.model[lang.code] = { display: false, value: '' };
      this.form.addControl(lang.code, new FormControl());
    }

    // support for ControlValueAccessor
    this.form.valueChanges.subscribe((value) => {
      if (this.onChange) {
        this.onChange(value);
      }
    });

    this.formSelector = this.fb.group({
      language: ['en'],
      text: [],
    });

    if (!this.enabled()) {
      this.disableForm();
    }

    this.formSelector.controls.language.valueChanges.subscribe((value) => {
      this.selectLang(value);
    });

    this.formSelector.controls.text.valueChanges.subscribe((value) => {
      this.updateModel(value);
    });
  }

  /**
   * Selects the active language and loads its stored value into the active
   * text field.
   *
   * @param s The language code to activate (case-insensitive).
   */
  public selectLang(s: string): void {
    this.lang.set(s.toLowerCase());
    this.formSelector.controls.text.patchValue(this.model[s].value);
  }

  /**
   * Stores the given text as the value for the currently active language and
   * patches the corresponding control in {@link form}.
   *
   * @param s The new text value for the active language.
   */
  public updateModel(s: string): void {
    this.model[this.lang()].value = s;
    this.form.controls[this.lang()].patchValue(this.model[this.lang()].value);
  }

  /**
   * Clears the value of a given language: hides its field, empties its stored
   * value and patches the corresponding form control to an empty string.
   *
   * @param lang The language code whose value should be removed.
   */
  public removeLangValue(lang: string) {
    this.model[lang].display = false;
    this.model[lang].value = '';
    this.form.controls[lang].patchValue('');
  }
}
