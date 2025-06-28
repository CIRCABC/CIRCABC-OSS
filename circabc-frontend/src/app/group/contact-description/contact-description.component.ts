import { Component, Input } from '@angular/core';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import { I18nSelectPipe } from '@angular/common';
import { type InterestGroup } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-contact-description',
  templateUrl: './contact-description.component.html',
  styleUrl: './contact-description.component.scss',
  preserveWhitespaces: true,
  imports: [I18nSelectPipe, TranslocoModule],
})
export class ContactDescriptionComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  public group!: InterestGroup;

  constructor(private translateService: TranslocoService) {}

  hasContact(): boolean {
    if (this.group?.contact) {
      return this.hasMLValue(this.group.contact);
    }
    return false;
  }

  hasMLValue(obj: { [key: string]: string }): boolean {
    if (obj) {
      const lang = this.getLang();
      if (obj[lang] !== undefined && obj[lang] !== '') {
        return true;
      }
    }

    return false;
  }

  getLang(): string {
    if (
      this.group?.description &&
      Object.keys(this.group.description).indexOf(
        this.translateService.getActiveLang()
      ) !== -1
    ) {
      return this.translateService.getActiveLang();
    }
    return this.translateService.getDefaultLang();
  }
}
