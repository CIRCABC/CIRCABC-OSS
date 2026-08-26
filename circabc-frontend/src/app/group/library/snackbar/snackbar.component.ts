import { NgClass } from '@angular/common';
import { Component, OnChanges, output, input } from '@angular/core';

@Component({
  selector: 'cbc-snackbar',
  templateUrl: './snackbar.component.html',
  styleUrl: './snackbar.component.scss',
  preserveWhitespaces: true,
  imports: [NgClass],
})
export class SnackbarComponent implements OnChanges {
  public readonly message = input.required<string>();
  public readonly duration = input(3000);
  public readonly snackFinished = output();
  public show = false;

  ngOnChanges() {
    this.showIt();
  }

  showIt() {
    this.show = true;
    setTimeout(() => {
      this.show = false;
      this.snackFinished.emit();
    }, this.duration());
  }
}
