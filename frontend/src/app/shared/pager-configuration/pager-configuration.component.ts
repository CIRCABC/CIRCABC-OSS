import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Renders the page-size configuration control for a pager, letting the user
 * choose how many items are displayed per page.
 *
 * This standalone component displays the selectable page-size options (defined
 * in its template) and notifies parent components of the user's selection via
 * the {@link PagerConfigurationComponent.pageSizeChanged} output. It holds no
 * internal state of its own; it simply relays the chosen page size upward so
 * the owning list/pager component can react.
 */
@Component({
  selector: 'cbc-pager-configuration',
  templateUrl: './pager-configuration.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class PagerConfigurationComponent {
  /**
   * Emits the newly selected page size (number of items per page) whenever the
   * user picks a different value. Parent components subscribe to this output to
   * update their pagination accordingly.
   */
  readonly pageSizeChanged = output<number>();

  /**
   * Handles a page-size selection by emitting it through
   * {@link PagerConfigurationComponent.pageSizeChanged}.
   *
   * @param pageSize The number of items per page chosen by the user.
   */
  public changePageSize(pageSize: number): void {
    this.pageSizeChanged.emit(pageSize);
  }
}
