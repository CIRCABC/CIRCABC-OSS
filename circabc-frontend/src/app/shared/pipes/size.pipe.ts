import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cbcSize',
  pure: true,
})
export class SizePipe implements PipeTransform {
  public transform(value: string | number | null, si: boolean): string {
    if (value) {
      let bytes: number;
      if (typeof value === 'string') {
        bytes = +value;
      } else {
        bytes = value;
      }
      const kilobyte = 1024;
      const thousand = 1000;
      const thresh = si ? thousand : kilobyte;
      if (Math.abs(bytes) < thresh) {
        return `${bytes} B`;
      }
      const units = si
        ? ['KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
        : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
      let u = -1;
      do {
        bytes /= thresh;
        u += 1;
      } while (Math.abs(bytes) >= thresh && u < units.length - 1);
      return `${
        bytes.toFixed(2).slice(-2) === '00' ? bytes : bytes.toFixed(2)
      } ${units[u]}`;
    }
    return '';
  }
}
