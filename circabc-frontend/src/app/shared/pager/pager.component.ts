import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'cbc-pager',
  templateUrl: './pager.component.html',
})
export class PagerComponent {
  @Input()
  pageIndex = 1;
  @Input()
  length = 0;
  @Input()
  pageSize = 0;
  @Output()
  readonly pageChanged: EventEmitter<number> = new EventEmitter<number>();
  public previousPage() {
    if (this.canPreviousPage()) {
      this.pageIndex--;
      this.pageChanged.emit(this.pageIndex);
    }
  }
  public canPreviousPage() {
    return this.pageIndex > 1;
  }

  public nextPage() {
    if (this.canNextPage()) {
      this.pageIndex++;
      this.pageChanged.emit(this.pageIndex);
    }
  }
  public canNextPage() {
    return this.pageIndex < Math.ceil(this.length / this.pageSize);
  }

  public goToPage(page: string) {
    this.pageIndex = Number(page);
    this.pageChanged.emit(this.pageIndex);
  }

  public getPages(): number[] {
    const result: number[] = [];

    for (let i = 1; i < this.length / this.pageSize + 1; i += 1) {
      result.push(i);
    }

    return result;
  }
}
