import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  forwardRef,
  inject,
  input,
  model,
  OnInit,
  output,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormBuilder,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatMenuModule } from '@angular/material/menu';
import { TranslocoModule } from '@jsverse/transloco';
import {
  availableLanguages,
  supportedLanguages,
} from 'app/shared/langs/supported-langs';

/**
 * Language selector component (`cbc-lang-selector`).
 *
 * Renders a menu-based language picker (using an Angular Material menu) that
 * lets the user choose a language from either the platform's supported
 * languages or the full set of worldwide languages. It integrates with
 * Angular reactive forms as a {@link ControlValueAccessor}, so it can be used
 * directly with `formControlName` / `ngModel` bindings, and additionally
 * exposes a two-way bindable {@link currentLang} model and a
 * {@link changedLang} output for imperative usage.
 *
 * The list of selectable languages is derived reactively via
 * {@link availableLang} from the {@link worldwide} and {@link disabledLangs}
 * inputs.
 */
@Component({
  selector: 'cbc-lang-selector',
  templateUrl: './lang-selector.component.html',
  styleUrl: './lang-selector.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      useExisting: forwardRef(() => LangSelectorComponent),
    },
  ],
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, TranslocoModule, MatMenuModule],
})
export class LangSelectorComponent implements OnInit, ControlValueAccessor {
  /** Reactive-forms builder used to create the internal {@link form}. */
  private readonly fb = inject(FormBuilder);

  /** Whether the selector menu/panel is currently expanded (open). */
  public expanded = false;
  /**
   * Internal reactive form holding the single `lang` control that backs the
   * selected language value. Initialized in {@link ngOnInit}.
   */
  public form!: FormGroup;

  /**
   * Two-way bindable model holding the currently selected language code.
   * Kept in sync with the form value via {@link writeValue}.
   */
  public currentLang = model<string>();
  /**
   * When `true`, the full set of worldwide languages is offered; otherwise
   * only the platform's supported languages are shown.
   */
  public readonly worldwide = input(false);
  /** When `true`, renders the selector in a more condensed (compact) layout. */
  public readonly compactMode = input(false);
  /** List of language codes to exclude from the selectable options. */
  public readonly disabledLangs = input<string[]>([]);
  /** Emits the newly selected language code whenever the language changes. */
  public readonly changedLang = output<string>();
  /** When `true`, the selector is rendered in a disabled (non-interactive) state. */
  public readonly disable = input(false);
  /** When `true`, forces the selector icon to use only the blue color variant. */
  public readonly iconColorOnlyBlue = input(false);
  /**
   * When `true`, hides the explicit "select a language" (no selection)
   * option from the list.
   */
  public readonly noSelectLangOption = input(false);

  /**
   * Reactively computed list of selectable languages. Chooses between
   * {@link availableLanguages} and {@link supportedLanguages} based on
   * {@link worldwide}, then filters out any codes listed in
   * {@link disabledLangs}.
   */
  public readonly availableLang = computed(() => {
    const source = this.worldwide() ? availableLanguages : supportedLanguages;
    const disabled = this.disabledLangs();
    return source.filter((lang) => !disabled.includes(lang.code));
  });

  /** Callback registered by the forms API, invoked when the value changes. */
  onChange: (value: string) => void = () => {};
  /** Callback registered by the forms API, invoked when the control is touched. */
  onTouched: () => void = () => {};

  /**
   * Guards the reset effect so it only runs after the form has been created,
   * preventing a spurious reset during initial setup.
   */
  private initialized = false;

  /**
   * Sets up an effect that resets the current selection whenever the set of
   * {@link availableLang} changes after the component has been initialized,
   * ensuring an option that is no longer available is not left selected.
   */
  constructor() {
    effect(() => {
      const langs = this.availableLang();
      if (langs && this.form && this.initialized) {
        this.onLanguageChange('');
      }
      if (this.form) {
        this.initialized = true;
      }
    });
  }

  /**
   * {@link ControlValueAccessor} hook: writes an externally provided value
   * into the form and syncs {@link currentLang}. A falsy value resets the
   * form and clears the current language.
   *
   * @param value The language code to set, or `null`/`undefined` to clear.
   */
  writeValue(value: string | null | undefined) {
    if (value) {
      this.form.controls['lang'].setValue(value);
      this.currentLang.set(value);
    } else {
      this.form.reset();
      this.currentLang.set('');
    }
  }

  /**
   * {@link ControlValueAccessor} hook: registers the callback invoked when
   * the selected value changes.
   *
   * @param fn Callback receiving the new language code.
   */
  registerOnChange(fn: (value: string) => void) {
    this.onChange = fn;
  }

  /**
   * {@link ControlValueAccessor} hook: registers the callback invoked when
   * the control is touched.
   *
   * @param fn Callback invoked on touch.
   */
  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  /**
   * Lifecycle hook: builds the internal {@link form} (with `updateOn: 'blur'`),
   * disables it when {@link disable} is set, and wires value changes through
   * to the registered {@link onChange} callback.
   */
  public ngOnInit(): void {
    this.form = this.fb.group({ lang: [] }, { updateOn: 'blur' });

    if (this.disable()) {
      this.form.disable();
    }

    this.form.valueChanges.subscribe((value) => {
      if (value.lang) {
        this.onChange(value.lang);
      }
    });
  }

  /**
   * Applies a language selection: notifies the forms API, updates the form
   * control, emits {@link changedLang}, and collapses the selector.
   *
   * @param value The selected language code (empty string clears the selection).
   */
  public onLanguageChange(value: string) {
    this.onChange(value);
    this.form.controls['lang'].setValue(value);
    this.changedLang.emit(value);
    this.expanded = false;
  }

  /** Opens the selector by setting {@link expanded} to `true`. */
  public expand() {
    this.expanded = true;
  }

  /** Closes the selector by setting {@link expanded} to `false`. */
  public collapse() {
    this.expanded = false;
  }
}
