import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
} from '@angular/core';

import {
  AuditService,
  LogSearchResult,
  PagedLogSearchResult,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.scss'],
  preserveWhitespaces: true,
})
export class HistoryComponent implements OnChanges {
  @Input()
  public showModal = false;
  @Output()
  public readonly modalHide = new EventEmitter();
  @Input()
  public itemId!: string;
  @Input()
  public historyText!: string;

  public historyEntries: LogSearchResult[] = [];

  public constructor(private auditService: AuditService) {}

  async ngOnChanges() {
    await this.loadHistory();
  }

  async loadHistory() {
    if (this.itemId !== undefined && this.showModal) {
      const results: PagedLogSearchResult = await firstValueFrom(
        this.auditService.getHistory(this.itemId, 0, 1)
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
