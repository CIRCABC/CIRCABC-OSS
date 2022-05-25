import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cbcTaggedToPlainText',
  pure: true,
})
export class TaggedToPlainTextPipe implements PipeTransform {
  public transform(value: string | undefined): string {
    return value ? String(value).replace(/<[^>]+>/gm, '') : '';
  }
}
