import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Presentational component that renders a horizontal loading indicator
 * (a progress/spinner bar defined in the associated template and stylesheet).
 *
 * It is a purely visual, stateless component used across the application to
 * signal that content is being loaded. It exposes no inputs or outputs and
 * holds no logic; its appearance is driven entirely by
 * `horizontal-loader.component.html` and `horizontal-loader.component.scss`.
 *
 * Rendered via the `cbc-horizontal-loader` selector. Change detection uses the
 * OnPush strategy and whitespace is preserved to keep the loader markup intact.
 */
@Component({
  selector: 'cbc-horizontal-loader',
  templateUrl: './horizontal-loader.component.html',
  styleUrl: './horizontal-loader.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class HorizontalLoaderComponent {}
