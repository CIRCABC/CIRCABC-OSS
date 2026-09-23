import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pure pipe (`cbcSize`) that formats a byte count into a human-readable
 * file-size string.
 *
 * It scales the raw byte value up through successive size units and appends
 * the appropriate suffix. The `si` flag selects between decimal (SI, base
 * 1000: KB, MB, GB, …) and binary (base 1024: KiB, MiB, GiB, …) units.
 * Values below the chosen threshold are rendered directly in bytes (`B`).
 *
 * @example
 * ```html
 * {{ 1536 | cbcSize:false }} <!-- "1.50 KiB" -->
 * {{ 1500 | cbcSize:true }}  <!-- "1.50 KB" -->
 * ```
 */
@Pipe({
  name: 'cbcSize',
  pure: true,
})
export class SizePipe implements PipeTransform {
  /**
   * Formats a byte count as a human-readable size string.
   *
   * @param value - The size in bytes, as a number or numeric string. A
   * falsy value (`null`, `0`, empty string) yields an empty string.
   * @param si - When `true`, uses decimal (SI) units with a base of 1000
   * (KB, MB, …); when `false`, uses binary units with a base of 1024
   * (KiB, MiB, …).
   * @returns The formatted size (e.g. `"1.50 KiB"`, `"512 B"`), or an empty
   * string when `value` is falsy.
   */
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
        bytes.toFixed(2).endsWith('00') ? bytes : bytes.toFixed(2)
      } ${units[u]}`;
    }
    return '';
  }
}
