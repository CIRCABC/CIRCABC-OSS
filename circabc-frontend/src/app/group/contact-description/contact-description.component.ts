import { Component, Input } from '@angular/core';

import { TranslocoService } from '@ngneat/transloco';

import { InterestGroup } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-contact-description',
  templateUrl: './contact-description.component.html',
  styleUrls: ['./contact-description.component.scss'],
  preserveWhitespaces: true,
})
export class ContactDescriptionComponent {
  @Input()
  public group!: InterestGroup;

  constructor(private translateService: TranslocoService) {}

  hasContact(): boolean {
    if (this.group && this.group.contact) {
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
      this.group &&
      this.group.description &&
      Object.keys(this.group.description).indexOf(
        this.translateService.getActiveLang()
      ) !== -1
    ) {
      return this.translateService.getActiveLang();
    } else {
      return this.translateService.getDefaultLang();
    }
  }
}
