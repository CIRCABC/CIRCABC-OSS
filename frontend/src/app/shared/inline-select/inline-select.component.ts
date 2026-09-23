import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { TranslocoService } from '@jsverse/transloco';

/**
 * Internal view-model describing a single selectable entry rendered as a
 * button-toggle option.
 */
interface SelectOption {
  /** The raw value emitted when the option is selected. */
  value: string;
  /** The human-readable, optionally translated, text shown to the user. */
  label: string;
}

/**
 * Standalone Angular component that renders a compact, inline set of mutually
 * exclusive choices as a Material button-toggle group.
 *
 * Each provided value becomes a toggle button. When a
 * `translationPrefix` is supplied, the displayed label for every value is
 * resolved through Transloco using the key `${translationPrefix}.${value}`;
 * otherwise the raw value is displayed. The current selection is backed by a
 * reactive form control, and any change is propagated to the parent through
 * the {@link InlineSelectComponent.selectionChanged} output.
 */
@Component({
  selector: 'cbc-inline-select',
  templateUrl: './inline-select.component.html',
  styleUrl: './inline-select.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatButtonToggleModule],
})
export class InlineSelectComponent implements OnInit {
  /** Reactive forms builder used to create the backing form group. */
  private readonly fb = inject(FormBuilder);
  /** Transloco service used to translate option labels when a prefix is set. */
  private readonly translateService = inject(TranslocoService);

  /**
   * Input. The list of raw values to render as selectable options. Each entry
   * produces one button in the toggle group. Defaults to an empty array.
   */
  readonly values = input<string[]>([]);
  /**
   * Input. Optional Transloco key prefix used to translate each value's label
   * via `${translationPrefix}.${value}`. When empty, the raw value is shown.
   */
  readonly translationPrefix = input('');
  /**
   * Input. The initially selected value. When non-empty, the corresponding
   * option is pre-selected on initialization.
   */
  readonly value = input('');
  /**
   * Output. Emits the newly selected value whenever the user changes the
   * selection.
   */
  readonly selectionChanged = output<string>();

  /** Reactive form group holding the single `selectValue` control. */
  public form!: FormGroup;
  /** The resolved list of options (value/label pairs) rendered by the view. */
  public options: SelectOption[] = [];

  /**
   * Angular lifecycle hook. Builds the option list, initializes the reactive
   * form, applies any initial {@link value}, and wires the control's value
   * changes to the {@link selectionChanged} output.
   */
  ngOnInit() {
    this.options = this.getValues();

    this.form = this.fb.group({
      selectValue: '',
    });

    const valueValue = this.value();
    if (valueValue !== '' && valueValue !== undefined) {
      this.form.controls.selectValue.patchValue(valueValue);
    }

    this.form.controls.selectValue.valueChanges.subscribe((value) => {
      this.selectionChanged.emit(value);
    });
  }

  /**
   * Builds the list of {@link SelectOption} entries from the {@link values}
   * input. When a {@link translationPrefix} is provided, each label is
   * translated through Transloco using `${translationPrefix}.${value}`;
   * otherwise the raw value is used as the label.
   *
   * @returns The resolved options, preserving the order of the input values.
   */
  public getValues(): SelectOption[] {
    const result: SelectOption[] = [];

    for (const v of this.values()) {
      let txt = v;

      const translationPrefix = this.translationPrefix();
      if (translationPrefix) {
        txt = this.translateService.translate(`${translationPrefix}.${v}`);
      }

      const item: SelectOption = {
        value: v,
        label: txt,
      };

      result.push(item);
    }

    return result;
  }
}
