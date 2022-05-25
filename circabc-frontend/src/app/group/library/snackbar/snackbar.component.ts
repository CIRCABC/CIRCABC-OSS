import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
} from '@angular/core';

@Component({
  selector: 'cbc-snackbar',
  templateUrl: './snackbar.component.html',
  styleUrls: ['./snackbar.component.scss'],
  preserveWhitespaces: true,
})
export class SnackbarComponent implements OnChanges {
  @Input()
  public message!: string;
  @Input()
  public duration = 3000;
  @Output()
  public readonly snackFinished = new EventEmitter();
  public show = false;

  ngOnChanges() {
    this.showIt();
  }

  showIt() {
    this.show = true;
    setTimeout(() => {
      this.show = false;
      this.snackFinished.emit();
    }, this.duration);
  }
}
