import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { environment } from 'environments/environment';

/**
 * Standalone component that renders the CIRCABC "About" help page.
 *
 * The template (`about.component.html`) presents static informational
 * content such as the current CIRCABC release and legal/privacy notices,
 * localised through Transloco. Navigation between the different informational
 * sections (e.g. privacy) is driven by the {@link AboutComponent.step} field.
 *
 * @remarks
 * Rendered via the `cbc-about` selector. It is a purely presentational
 * component with no `input()` or `output()` members.
 */
@Component({
  selector: 'cbc-about',
  templateUrl: './about.component.html',
  styleUrl: './about.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class AboutComponent {
  /**
   * The CIRCABC release identifier taken from the active environment
   * configuration, displayed to the user on the about page.
   */
  public circabcRelease = environment.circabcRelease;

  /**
   * Identifier of the currently selected informational section shown on the
   * page. Defaults to `'privacy'`.
   */
  public step = 'privacy';
}
