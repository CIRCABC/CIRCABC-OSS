import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { environment } from 'environments/environment';

/**
 * Standalone presentational component that renders the European Commission
 * logo for the CIRCABC application.
 *
 * The component's template displays the EC/CIRCABC branding, adapting the
 * logo to the currently active UI language and exposing the current CIRCABC
 * release version (typically used for cache-busting logo assets or for
 * display purposes).
 *
 * Selector: `cbc-ec-logo-app`.
 *
 * It relies on the {@link TranslocoService} to determine the active language
 * and on the application `environment` configuration to obtain the release
 * identifier.
 */
@Component({
  selector: 'cbc-ec-logo-app',
  templateUrl: './ec-logo-app.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './ec-logo-app.component.scss',
})
export class EcLogoAppComponent {
  /**
   * Transloco service used to read the currently active UI language so the
   * rendered logo can be localised.
   */
  private readonly translateService = inject(TranslocoService);

  /**
   * Current CIRCABC release identifier, taken from the application
   * `environment` configuration. Exposed to the template for display and/or
   * asset versioning.
   */
  public circabcRelease = environment.circabcRelease;

  /**
   * Returns the currently active UI language code in lower case.
   *
   * The value is resolved from the {@link TranslocoService} active language
   * and is used by the template to select the language-specific variant of
   * the logo.
   *
   * @returns The active language code normalised to lower case (e.g. `"en"`).
   */
  public getLang(): string {
    return this.translateService.getActiveLang().toLocaleLowerCase();
  }
}
