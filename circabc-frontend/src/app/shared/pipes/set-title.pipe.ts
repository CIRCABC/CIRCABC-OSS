import { Pipe, PipeTransform } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Pipe({
  name: 'cbcSetTitle',
  // eslint-disable-next-line @angular-eslint/no-pipe-impure
  pure: false,
})
export class SetTitlePipe implements PipeTransform {
  constructor(private title: Title) {}

  public transform(value: string | undefined): string {
    if (value !== undefined) {
      this.title.setTitle(value);
    }
    return '';
  }
}
