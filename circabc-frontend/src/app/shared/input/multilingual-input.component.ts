/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  forwardRef,
  input,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormBuilder,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';

import { nonEmptyTitle } from 'app/core/validation.service';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import {
  LanguageCodeName,
  supportedLanguages,
} from 'app/shared/langs/supported-langs';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';

@Component({
  selector: 'cbc-multilingual-input',
  templateUrl: './multilingual-input.component.html',
  styleUrl: './multilingual-input.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      // eslint-disable-next-line @angular-eslint/no-forward-ref
      useExisting: forwardRef(() => MultilingualInputComponent),
    },
  ],
  preserveWhitespaces: false,
  imports: [ReactiveFormsModule, DataCyDirective, EditorModule, SharedModule],
})
export class MultilingualInputComponent
  implements OnInit, ControlValueAccessor, OnChanges
{
  form!: FormGroup;
  formSelector!: FormGroup;

  readonly value = input<string>();
  readonly placeholder = input<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  lang = 'en';
  readonly label = input.required<string>();
  readonly textarea = input(false);
  readonly enabled = input(true);
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showTranslationPanel = false;
  readonly required = input(false);

  public defaultLang = 'en';
  public availableLangs!: LanguageCodeName[];
  public selectAll = false;

  public model: { [key: string]: { display: boolean; value: string } } = {};

  constructor(private fb: FormBuilder) {}

  // impement ControlValueAccessor interface
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  // eslint-disable-next-line no-empty,@typescript-eslint/no-empty-function
  onChange = (_: any) => {};
  // eslint-disable-next-line no-empty, @typescript-eslint/no-empty-function
  onTouched = () => {};

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  writeValue(values: any) {
    this.showTranslationPanel = true;
    if (this.model && this.form) {
      for (const lang of this.availableLangs) {
        const mustDisplay = !(
          values === null ||
          values[lang.code] === undefined ||
          values[lang.code] === ''
        );
        const val =
          values === null ||
          values[lang.code] === undefined ||
          values[lang.code] === null
            ? ''
            : values[lang.code];
        this.model[lang.code] = { display: mustDisplay, value: val };
        this.form.controls[lang.code].patchValue(val);
      }

      if (this.formSelector) {
        this.formSelector.controls.text.patchValue(this.model[this.lang].value);
      }
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  public disableForm() {
    this.form.disable();
    this.formSelector.controls.text.disable();
  }

  public enableForm() {
    this.form.enable();
    this.formSelector.controls.text.enable();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.enabled && this.form) {
      if (changes.enabled.currentValue) {
        this.enableForm();
      } else {
        this.disableForm();
      }
    }
  }

  ngOnInit() {
    this.initForm();
  }

  private initForm() {
    this.lang = this.lang.toLowerCase();
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

  public selectLang(s: string): void {
    this.lang = s.toLowerCase();
    this.formSelector.controls.text.patchValue(this.model[s].value);
  }

  public updateModel(s: string): void {
    this.model[this.lang].value = s;
    this.form.controls[this.lang].patchValue(this.model[this.lang].value);
  }

  public removeLangValue(lang: string) {
    this.model[lang].display = false;
    this.model[lang].value = '';
    this.form.controls[lang].patchValue('');
  }
}
