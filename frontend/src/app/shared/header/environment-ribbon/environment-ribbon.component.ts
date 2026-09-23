import { ChangeDetectionStrategy, Component } from '@angular/core';
import { environment } from 'environments/environment';

// to keep in mind
// http://www.cssportal.com/css-ribbon-generator/

/**
 * Displays a corner ribbon banner indicating the current deployment
 * environment (for example: test, acceptance, training).
 *
 * The component renders a styled ribbon overlay whose label/appearance is
 * driven by the {@link environmentType} value read from the active build
 * environment configuration. It is typically shown in the application header
 * so users can immediately tell which environment (non-production target)
 * they are currently working in.
 *
 * @remarks
 * Uses {@link ChangeDetectionStrategy.OnPush} because its state is derived from
 * a static environment value and does not change at runtime.
 */
@Component({
  selector: 'cbc-environment-ribbon',
  templateUrl: './environment-ribbon.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './environment-ribbon.component.scss',
})
export class EnvironmentRibbonComponent {
  /**
   * The current environment identifier taken from the active build's
   * `environment` configuration. Used by the template to decide whether and
   * how to render the ribbon (its label and styling per deployment target).
   */
  public environmentType = environment.environmentType;
}
