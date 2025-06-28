import { Component, Input, OnInit, output, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'cbc-overlayer',
  templateUrl: './overlayer.component.html',
  styleUrl: './overlayer.component.scss',
  preserveWhitespaces: true,
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class OverlayerComponent implements OnInit {
  readonly enabledClose = input(true);
  readonly useKeepDisplay = input(false);
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  visible = false;

  public readonly closed = output<boolean>();

  public form!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.form = this.fb.group({
      doNotShow: [false],
    });
  }

  public close() {
    this.visible = false;
    this.closed.emit(this.form.value.doNotShow);
  }
}
