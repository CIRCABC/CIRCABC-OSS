import { Component, output } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'cbc-pager-configuration',
  templateUrl: './pager-configuration.component.html',
  imports: [TranslocoModule],
})
export class PagerConfigurationComponent {
  readonly pageSizeChanged = output<number>();

  public changePageSize(pageSize: number): void {
    this.pageSizeChanged.emit(pageSize);
  }
}
