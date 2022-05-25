import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'cbc-pager-configuration',
  templateUrl: './pager-configuration.component.html',
})
export class PagerConfigurationComponent {
  @Output()
  readonly pageSizeChanged: EventEmitter<number> = new EventEmitter<number>();

  public changePageSize(pageSize: number): void {
    this.pageSizeChanged.emit(pageSize);
  }
}
