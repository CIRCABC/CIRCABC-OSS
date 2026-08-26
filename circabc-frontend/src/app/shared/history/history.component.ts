import { Component, Input, OnChanges, output, input } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import {
  AuditService,
  LogSearchResult,
  PagedLogSearchResult,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-history',
  templateUrl: './history.component.html',
  styleUrl: './history.component.scss',
  preserveWhitespaces: true,
  imports: [TranslocoModule],
})
export class HistoryComponent implements OnChanges {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public showModal = false;
  public readonly modalHide = output();
  public readonly itemId = input.required<string>();
  public readonly historyText = input.required<string>();

  public historyEntries: LogSearchResult[] = [];

  public constructor(private auditService: AuditService) {}

  async ngOnChanges() {
    await this.loadHistory();
  }

  async loadHistory() {
    const itemId = this.itemId();
    if (itemId !== undefined && this.showModal) {
      const results: PagedLogSearchResult = await firstValueFrom(
        this.auditService.getHistory(itemId, 0, 1)
      );
      if (results.data !== undefined) {
        this.historyEntries = results.data;
      } else {
        this.historyEntries = [];
      }
    }
  }

  public close() {
    this.showModal = false;
    this.modalHide.emit();
  }
}
