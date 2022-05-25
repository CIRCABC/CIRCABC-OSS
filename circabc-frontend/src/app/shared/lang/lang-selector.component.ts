/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  Component,
  ElementRef,
  EventEmitter,
  forwardRef,
  HostListener,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormBuilder,
  FormGroup,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';
import {
  LanguageCodeName,
  SupportedLangs,
  WorldLangs,
} from 'app/shared/langs/supported-langs';

@Component({
  selector: 'cbc-lang-selector',
  templateUrl: './lang-selector.component.html',
  styleUrls: ['./lang-selector.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      // eslint-disable-next-line @angular-eslint/no-forward-ref
      useExisting: forwardRef(() => LangSelectorComponent),
    },
  ],
  preserveWhitespaces: true,
})
export class LangSelectorComponent
  implements OnInit, OnChanges, ControlValueAccessor
{
  public availableLang: LanguageCodeName[] = [];
  public expanded = false;
  public form!: FormGroup;
  private elementRef: ElementRef;

  @Input()
  public currentLang!: string;
  // false = restricted to EU list of languages, true = world languages
  @Input()
  public worldwide = false;
  @Input()
  public compactMode = false;
  @Input()
  public disabledLangs: string[] = [];
  @Output()
  public readonly changedLang: EventEmitter<string> = new EventEmitter();
  @Output()
  public readonly clickOutside = new EventEmitter<MouseEvent>();
  @Input()
  public disable = false;

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

  constructor(private fb: FormBuilder, myElement: ElementRef) {
    this.elementRef = myElement;
  }

  public ngOnInit(): void {
    this.form = this.fb.group(
      {
        lang: [],
      },
      {
        updateOn: 'blur',
      }
    );

    if (this.disable) {
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
    if (this.worldwide) {
      for (const lang of WorldLangs.availableLangs) {
        if (this.disabledLangs.indexOf(lang.code) === -1) {
          this.availableLang.push(lang);
        }
      }
    } else {
      for (const lang of SupportedLangs.availableLangs) {
        if (this.disabledLangs.indexOf(lang.code) === -1) {
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

  @HostListener('document:click', ['$event', '$event.target'])
  public onClick(event: MouseEvent, targetElement: HTMLElement): void {
    if (!targetElement) {
      return;
    }

    const clickedInside = this.elementRef.nativeElement.contains(targetElement);
    if (targetElement.className === 'iconWorldToggle') {
      this.expanded = !this.expanded;
    } else if (!clickedInside) {
      this.clickOutside.emit(event);
      this.expanded = false;
    }
  }
}
