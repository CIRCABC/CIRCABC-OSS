import { NgClass } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  input,
  OnChanges,
  output,
  signal,
} from '@angular/core';

/**
 * Transient snackbar/toast component used in the library area.
 *
 * Renders a short-lived notification message (styled via {@link NgClass}
 * against the `show` flag) that automatically becomes visible when its
 * inputs change and hides again after a configurable delay. Once the
 * message has been dismissed it notifies the parent through the
 * {@link SnackbarComponent.snackFinished} output so the host can clean up
 * or display the next message.
 */
@Component({
  selector: 'cbc-snackbar',
  templateUrl: './snackbar.component.html',
  styleUrl: './snackbar.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgClass],
})
export class SnackbarComponent implements OnChanges {
  /**
   * Required text content displayed inside the snackbar.
   *
   * @input
   */
  public readonly message = input.required<string>();
  /**
   * Time in milliseconds the snackbar stays visible before it is
   * automatically hidden. Defaults to `3000` (3 seconds).
   *
   * @input
   */
  public readonly duration = input(3000);
  /**
   * Emits once the snackbar has finished displaying (after the
   * {@link SnackbarComponent.duration} elapses and the snackbar is hidden),
   * allowing the parent to react to the dismissal.
   *
   * @output
   */
  public readonly snackFinished = output();
  /**
   * Visibility flag bound to the template; `true` while the snackbar is
   * displayed and `false` once it has been dismissed.
   */
  public readonly show = signal(false);

  /**
   * Angular lifecycle hook triggered whenever a bound input changes.
   * Re-displays the snackbar by delegating to {@link SnackbarComponent.showIt}.
   */
  ngOnChanges() {
    this.showIt();
  }

  /**
   * Shows the snackbar and schedules its automatic dismissal.
   *
   * Sets {@link SnackbarComponent.show} to `true`, then after
   * {@link SnackbarComponent.duration} milliseconds hides it again and emits
   * the {@link SnackbarComponent.snackFinished} event.
   */
  showIt() {
    this.show.set(true);
    setTimeout(() => {
      this.show.set(false);
      this.snackFinished.emit();
    }, this.duration());
  }
}
