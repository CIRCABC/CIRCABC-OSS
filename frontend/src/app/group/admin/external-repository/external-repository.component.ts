import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { ExternalRepositoryHistoryComponent } from './ext-repo-history/cbc-external-repository-history.component';
import { ExternalRepositoryPropertiesComponent } from './ext-repo-properties/external-repository-properties.component';

/**
 * Group administration component for managing an external repository.
 *
 * Renders the external repository administration view (selector
 * `cbc-external-repository`) as a tabbed container that toggles between two
 * child sections:
 * - the repository properties editor
 *   ({@link ExternalRepositoryPropertiesComponent}), and
 * - the repository connection/action history
 *   ({@link ExternalRepositoryHistoryComponent}).
 *
 * The component itself holds no repository data; it only tracks which of the
 * two sub-views is currently active and exposes methods for the template to
 * switch between them.
 */
@Component({
  selector: 'cbc-external-repository',
  templateUrl: './external-repository.component.html',
  styleUrl: './external-repository.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ExternalRepositoryPropertiesComponent,
    ExternalRepositoryHistoryComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class ExternalRepositoryComponent {
  /**
   * Whether the properties section is currently visible.
   * Defaults to `true` so the properties view is shown on load.
   */
  public showPropertiesItems = true;

  /**
   * Whether the history section is currently visible.
   * Defaults to `false` so the history view is hidden on load.
   */
  public showHistoryItems = false;

  /**
   * Indicates whether the properties section should be rendered.
   *
   * @returns `true` when the properties view is active, otherwise `false`.
   */
  public isShowProperties(): boolean {
    return this.showPropertiesItems;
  }

  /**
   * Indicates whether the history section should be rendered.
   *
   * @returns `true` when the history view is active, otherwise `false`.
   */
  public isShowHistory(): boolean {
    return this.showHistoryItems;
  }

  /**
   * Activates the properties section and hides the history section.
   */
  public showProperties() {
    this.showPropertiesItems = true;
    this.showHistoryItems = false;
  }

  /**
   * Activates the history section and hides the properties section.
   */
  public showHistory() {
    this.showPropertiesItems = false;
    this.showHistoryItems = true;
  }
}
