/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  forwardRef,
  output,
  input,
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
  LanguageCodeName,
  availableLanguages,
  supportedLanguages,
} from 'app/shared/langs/supported-langs';

@Component({
  selector: 'cbc-lang-selector',
  templateUrl: './lang-selector.component.html',
  styleUrl: './lang-selector.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      // eslint-disable-next-line @angular-eslint/no-forward-ref
      useExisting: forwardRef(() => LangSelectorComponent),
    },
  ],
  preserveWhitespaces: true,
  imports: [ReactiveFormsModule, TranslocoModule, MatMenuModule],
})
export class LangSelectorComponent
  implements OnInit, OnChanges, ControlValueAccessor
{
  public availableLang: LanguageCodeName[] = [];
  public expanded = false;
  public form!: FormGroup;

  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public currentLang!: string;
  // false = restricted to EU list of languages, true = world languages
  public readonly worldwide = input(false);
  public readonly compactMode = input(false);
  public readonly disabledLangs = input<string[]>([]);
  public readonly changedLang = output<string>();
  public readonly clickOutside = output<MouseEvent>();
  public readonly disable = input(false);
  public readonly iconColorOnlyBlue = input(false);
  public readonly noSelectLangOption = input(false);

  // impement ControlValueAccessor interface
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  // eslint-disable-next-line no-empty,@typescript-eslint/no-empty-function
  onChange = (_: any) => {};
  // eslint-disable-next-line no-empty, @typescript-eslint/no-empty-function
  onTouched = () => {};

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  writeValue(value: any) {
    if (value !== undefined && value !== null && value !== '') {
      this.form.controls.lang.setValue(value);
      this.currentLang = value;
    } else {
      this.form.reset();
      this.currentLang = '';
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  constructor(private fb: FormBuilder) {}

  public ngOnInit(): void {
    this.form = this.fb.group(
      {
        lang: [],
      },
      {
        updateOn: 'blur',
      }
    );

    if (this.disable()) {
      this.form.disable();
    }

    this.form.valueChanges.subscribe((value) => {
      if (this.onChange && value.lang) {
        this.onChange(value.lang);
      }
    });

    this.buildAvailableLands();
  }

  public ngOnChanges(changes: SimpleChanges) {
    if (changes.disabledLangs && !changes.disabledLangs.firstChange) {
      this.buildAvailableLands();
      this.onLanguageChange('');
    }
  }

  private buildAvailableLands() {
    this.availableLang = [];
    if (this.worldwide()) {
      for (const lang of availableLanguages) {
        if (this.disabledLangs().indexOf(lang.code) === -1) {
          this.availableLang.push(lang);
        }
      }
    } else {
      for (const lang of supportedLanguages) {
        if (this.disabledLangs().indexOf(lang.code) === -1) {
          this.availableLang.push(lang);
        }
      }
    }
  }

  public onLanguageChange(value: string) {
    if (this.onChange) {
      this.onChange(value);
    }
    this.form.controls.lang.setValue(value);
    this.changedLang.emit(value);
    this.expanded = false;
  }

  public expand() {
    this.expanded = true;
  }

  public collapse() {
    this.expanded = false;
  }
}
