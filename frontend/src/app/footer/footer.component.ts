import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  APP_ALF_VERSION,
  APP_VERSION,
  BUILD_DATE,
  NODE_NAME,
} from 'app/core/variables';
import { environment } from 'environments/environment';

/**
 * Application footer component (`cbc-footer`).
 *
 * Renders the CIRCABC footer bar shown across the application, displaying
 * version and build information (CIRCABC release, Alfresco backend version,
 * frontend application version, serving node name and build date) alongside
 * router-linked footer navigation and localized labels.
 *
 * Version metadata is resolved once at construction time from the
 * application-level injection tokens ({@link APP_ALF_VERSION},
 * {@link APP_VERSION}, {@link NODE_NAME}, {@link BUILD_DATE}), while the
 * CIRCABC release string is read directly from the build environment.
 *
 * @remarks
 * Uses {@link RouterLink} for footer navigation and {@link TranslocoModule}
 * for internationalized text.
 */
@Component({
  selector: 'cbc-footer',
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TranslocoModule],
})
export class FooterComponent {
  /** CIRCABC release identifier, read from the active build environment. */
  public circabcRelease = environment.circabcRelease;
  /** Version of the underlying Alfresco backend; empty when not provided. */
  public appAlfVersion = '';
  /** Version of the CIRCABC frontend application; empty when not provided. */
  public appVersion = '';
  /** Name of the node currently serving the application; empty when not provided. */
  public nodeName = '';
  /** Build date of the running application; empty when not provided. */
  public buildDate = '';

  /**
   * Resolves version and build metadata from the application injection tokens
   * and populates the corresponding fields.
   *
   * Each token is injected and, when it resolves to a truthy value, copied to
   * its matching public field; otherwise the field keeps its default empty
   * string so the footer renders gracefully when metadata is unavailable.
   */
  constructor() {
    const appAlfVersion = inject(APP_ALF_VERSION);
    const appVersion = inject(APP_VERSION);
    const nodeName = inject(NODE_NAME);
    const buildDate = inject(BUILD_DATE);

    if (appAlfVersion) {
      this.appAlfVersion = appAlfVersion;
    }
    if (appVersion) {
      this.appVersion = appVersion;
    }
    if (nodeName) {
      this.nodeName = nodeName;
    }
    if (buildDate) {
      this.buildDate = buildDate;
    }
  }
}
