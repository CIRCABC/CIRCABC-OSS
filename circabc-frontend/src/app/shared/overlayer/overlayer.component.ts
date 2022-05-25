import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'cbc-overlayer',
  templateUrl: './overlayer.component.html',
  styleUrls: ['./overlayer.component.scss'],
  preserveWhitespaces: true,
})
export class OverlayerComponent implements OnInit {
  @Input()
  enabledClose = true;
  @Input()
  useKeepDisplay = false;
  @Input()
  visible = false;

  @Output()
  public readonly closed = new EventEmitter();

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
