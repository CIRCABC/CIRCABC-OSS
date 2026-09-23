import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Presentational component (`cbc-flat-message`) that renders a single,
 * flat (non-dismissible, static) message to the user.
 *
 * It simply displays the text provided through its {@link FlatMessageComponent.message}
 * input using the associated template and styles. The component holds no
 * internal state and performs no logic; it acts as a lightweight, reusable
 * building block for surfacing short informational messages within the UI.
 *
 * `preserveWhitespaces` is enabled so that any significant whitespace in the
 * template is retained when rendering the message.
 */
@Component({
  selector: 'cbc-flat-message',
  templateUrl: './flat-message.component.html',
  styleUrl: './flat-message.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class FlatMessageComponent {
  /**
   * Required input carrying the text to display.
   *
   * Declared as a signal input via `input.required<string>()`, so a value
   * must be supplied by the consuming template; reading the signal returns
   * the current message string.
   */
  public readonly message = input.required<string>();
}
