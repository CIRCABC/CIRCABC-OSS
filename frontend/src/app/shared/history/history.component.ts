import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnChanges,
  output,
  signal,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  AuditService,
  LogSearchResult,
  PagedLogSearchResult,
} from 'app/core/generated/circabc';

/**
 * Modal component that displays the audit/change history of a given item.
 *
 * Rendered as a `cbc-history` element, it shows a dialog (controlled by
 * {@link HistoryComponent.showModal}) listing the most recent audit log
 * entries retrieved for the item identified by {@link HistoryComponent.itemId}.
 * History data is fetched from the backend through the generated
 * {@link AuditService}.
 *
 * The component reacts to input changes via {@link HistoryComponent.ngOnChanges},
 * reloading the history whenever the bound inputs (or the modal visibility)
 * change.
 */
@Component({
  selector: 'cbc-history',
  templateUrl: './history.component.html',
  styleUrl: './history.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class HistoryComponent implements OnChanges {
  /** Generated backend client used to fetch the item's audit history. */
  private readonly auditService = inject(AuditService);

  /**
   * Two-way bound model controlling the modal's visibility.
   * `true` shows the history dialog, `false` hides it.
   */
  public showModal = model<boolean>(false);
  /** Emitted when the modal is closed, allowing the parent to react to dismissal. */
  public readonly modalHide = output();
  /** Required identifier of the item whose history should be displayed. */
  public readonly itemId = input.required<string>();
  /** Required label/title text shown in the history dialog header. */
  public readonly historyText = input.required<string>();

  /** The list of audit log entries currently displayed in the dialog. */
  public readonly historyEntries = signal<LogSearchResult[]>([]);

  /**
   * Angular lifecycle hook triggered on any bound input change.
   * Reloads the item's history so the displayed entries stay in sync.
   *
   * @returns A promise that resolves once the history has been (re)loaded.
   */
  ngOnChanges() {
    void this.handleChanges();
  }

  private async handleChanges() {
    await this.loadHistory();
  }

  /**
   * Loads the most recent audit history entry for the current {@link itemId}.
   *
   * The request is only performed when an item id is present and the modal is
   * visible. On success {@link historyEntries} is populated with the returned
   * data, or reset to an empty array when the response contains no data.
   *
   * @returns A promise that resolves once {@link historyEntries} has been updated.
   */
  async loadHistory() {
    const itemId = this.itemId();
    if (itemId !== undefined && this.showModal()) {
      const results: PagedLogSearchResult =
        await this.auditService.getHistoryAsync({
          id: itemId,
          limit: 0,
          page: 1,
        });
      if (results.data === undefined) {
        this.historyEntries.set([]);
      } else {
        this.historyEntries.set(results.data);
      }
    }
  }

  /**
   * Closes the history dialog.
   *
   * Sets {@link showModal} to `false` and emits {@link modalHide} to notify
   * the parent component of the dismissal.
   */
  public close() {
    this.showModal.set(false);
    this.modalHide.emit();
  }
}
