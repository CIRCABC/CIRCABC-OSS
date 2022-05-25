import { Injectable, Pipe, PipeTransform } from '@angular/core';

import { TranslocoService } from '@ngneat/transloco';

@Pipe({
  name: 'cbcI18n',
  // eslint-disable-next-line @angular-eslint/no-pipe-impure
  pure: false,
})
@Injectable()
export class I18nPipe implements PipeTransform {
  public constructor(private translateService: TranslocoService) {}

  transform(mltext: { [key: string]: string } | undefined): string {
    if (mltext === undefined) {
      return '';
    }
    let result = '';
    const lang = this.translateService.getActiveLang();
    const defaultLang = this.translateService.getDefaultLang();

    if (this.countNonEmptyValues(mltext) === 1) {
      result = this.getValue(mltext, this.getOnlyOneValueKey(mltext));
    } else if (
      this.countNonEmptyValues(mltext) > 1 &&
      this.hasLocale(mltext, lang)
    ) {
      result = this.getValue(mltext, lang);
    } else if (
      this.countNonEmptyValues(mltext) > 1 &&
      this.hasLocale(mltext, defaultLang)
    ) {
      result = this.getValue(mltext, defaultLang);
    } else {
      result = this.getFirstValidValue(mltext);
    }
    return result;
  }

  private hasLocale(
    mltext: { [key: string]: string },
    locale: string
  ): boolean {
    const keys = Object.keys(mltext);
    const firstLook = keys.includes(locale);

    // fix, we search for 'xx-', because some old values from CIRCABC has title saved like { en-US: 'eeeee', fr-FR: 'ffff', es: 'erjlkg'}
    if (!firstLook && locale.indexOf('-') === -1) {
      return keys.includes(`${locale}-`);
    } else {
      return keys.includes(locale);
    }
  }

  private countNonEmptyValues(mltext: { [key: string]: string }): number {
    let i = 0;
    const keys = Object.keys(mltext);

    for (const k of keys) {
      if (this.getValue(mltext, k) !== '') {
        i += 1;
      }
    }

    return i;
  }

  private getOnlyOneValueKey(mltext: { [key: string]: string }): string {
    let result = '';
    const keys = Object.keys(mltext);

    for (const k of keys) {
      if (this.getValue(mltext, k) !== '') {
        result = k;
      }
    }

    return result;
  }

  private getFirstValidValue(mltext: { [key: string]: string }): string {
    let result = '';
    const keys = Object.keys(mltext);

    for (const k of keys) {
      if (this.getValue(mltext, k) !== '') {
        result = this.getValue(mltext, k);
        break;
      }
    }

    return result;
  }

  private getValue(mltext: { [key: string]: string }, locale: string): string {
    const keys = Object.keys(mltext);
    const firstLook = keys.includes(locale);
    let resultKey;

    // fix, we search for 'xx-', because some old values from CIRCABC has title saved like { en-US: 'eeeee', fr-FR: 'ffff', es: 'erjlkg'}
    if (!firstLook && locale.indexOf('-') === -1) {
      resultKey = keys.find((key) => key.indexOf(`${locale}-`) !== -1);
    } else {
      resultKey = keys.find((key) => key.indexOf(locale) !== -1);
    }

    if (resultKey !== undefined) {
      return mltext[resultKey] !== undefined ? mltext[resultKey] : '';
    } else {
      return '';
    }
  }
}
