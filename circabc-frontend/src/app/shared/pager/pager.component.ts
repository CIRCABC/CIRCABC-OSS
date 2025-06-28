import { Component, input, model } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'cbc-pager',
  templateUrl: './pager.component.html',
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class PagerComponent {
  page = model<number>(1);
  readonly length = input(0);
  readonly pageSize = input(0);
  public previousPage() {
    if (this.canPreviousPage()) {
      this.page.update((value) => value - 1);
    }
  }
  public canPreviousPage() {
    return this.page() > 1;
  }

  public nextPage() {
    if (this.canNextPage()) {
      this.page.update((value) => value + 1);
    }
  }
  public canNextPage() {
    return this.page() < Math.ceil(this.length() / this.pageSize());
  }

  public goToPage(page: string) {
    this.page.set(Number(page));
  }

  public getPages(): number[] {
    const result: number[] = [];

    for (let i = 1; i < this.length() / this.pageSize() + 1; i += 1) {
      result.push(i);
    }

    return result;
  }
}
