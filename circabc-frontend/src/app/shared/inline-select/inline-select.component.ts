import { Component, OnInit, output, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoService } from '@jsverse/transloco';
import { SelectItem } from 'primeng/api/selectitem';
import { SelectButtonModule } from 'primeng/selectbutton';
@Component({
  selector: 'cbc-inline-select',
  templateUrl: './inline-select.component.html',
  styleUrl: './inline-select.component.scss',
  preserveWhitespaces: true,
  imports: [ReactiveFormsModule, SelectButtonModule],
})
export class InlineSelectComponent implements OnInit {
  readonly values = input<string[]>([]);
  readonly translationPrefix = input('');
  readonly value = input('');
  readonly selectionChanged = output<string>();

  public form!: FormGroup;
  public options: SelectItem[] = [];

  constructor(
    private fb: FormBuilder,
    private translateService: TranslocoService
  ) {}

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

  public getValues(): SelectItem[] {
    const result: SelectItem[] = [];

    for (const v of this.values()) {
      let txt = v;

      const translationPrefix = this.translationPrefix();
      if (translationPrefix) {
        txt = this.translateService.translate(`${translationPrefix}.${v}`);
      }

      const item: SelectItem = {
        value: v,
        label: txt,
      };

      result.push(item);
    }

    return result;
  }
}
