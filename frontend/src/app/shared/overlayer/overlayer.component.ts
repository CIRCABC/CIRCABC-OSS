import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnInit,
  output,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Overlay dialog component (`cbc-overlayer`).
 *
 * Renders a dismissible overlay/modal container whose visibility is
 * controlled through a two-way bound `visible` model. It projects arbitrary
 * content and optionally exposes a close control together with a
 * "do not show again" checkbox (via a reactive form) so that callers can
 * persist the user's preference to keep the overlay hidden in the future.
 *
 * The component is standalone and relies on `ReactiveFormsModule` for its
 * internal form and `TranslocoModule` for i18n of the rendered labels.
 */
@Component({
  selector: 'cbc-overlayer',
  templateUrl: './overlayer.component.html',
  styleUrl: './overlayer.component.scss',
  preserveWhitespaces: true,
  imports: [ReactiveFormsModule, TranslocoModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OverlayerComponent implements OnInit {
  /** Angular `FormBuilder` used to construct the internal reactive form. */
  private readonly fb = inject(FormBuilder);

  /**
   * Input controlling whether the close control is available to the user.
   * When `true` (default) the overlay can be dismissed; when `false` the
   * close affordance is hidden/disabled.
   */
  readonly enabledClose = input(true);
  /**
   * Input toggling the "keep display" behavior, i.e. whether the overlay
   * offers the "do not show again" option to the user. Defaults to `false`.
   */
  readonly useKeepDisplay = input(false);

  /**
   * Two-way bindable model reflecting the overlay's visibility state.
   * `true` shows the overlay, `false` hides it. Defaults to `false`.
   */
  visible = model(false);

  /**
   * Output emitted when the overlay is closed. The emitted boolean carries
   * the current value of the "do not show" checkbox, allowing the parent to
   * persist whether the overlay should remain hidden going forward.
   */
  public readonly closed = output<boolean>();

  /**
   * Reactive form backing the overlay. Contains a single `doNotShow`
   * control. Initialized in {@link OverlayerComponent.ngOnInit}.
   */
  public form!: FormGroup;

  /**
   * Angular lifecycle hook. Builds the reactive {@link form} with a single
   * `doNotShow` control defaulting to `false`.
   */
  ngOnInit() {
    this.form = this.fb.group({
      doNotShow: [false],
    });
  }

  /**
   * Closes the overlay by setting {@link visible} to `false` and emitting the
   * {@link closed} output with the current `doNotShow` form value.
   */
  public close() {
    this.visible.set(false);
    this.closed.emit(this.form.value.doNotShow);
  }
}
