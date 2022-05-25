import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cbcCapitalize',
  pure: true,
})
export class CapitalizePipe implements PipeTransform {
  public transform(value: string): string {
    let res = '';
    if (value) {
      const parts = value.split(' ');
      for (const part of parts) {
        res = ` ${part.charAt(0).toUpperCase()}${part.slice(1)}`;
      }
    }
    return res.trim();
  }
}
