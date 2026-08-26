import { Component, Input, OnInit, viewChild, input } from '@angular/core';

import { LoginService } from 'app/core/login.service';
import { type CardEntry } from 'app/core/ui-model/card-entry';
import { SafePipe } from 'app/shared/pipes/safe.pipe';

@Component({
  selector: 'cbc-card',
  templateUrl: './card.component.html',
  styleUrl: './card.component.scss',
  preserveWhitespaces: true,
  imports: [SafePipe],
})
export class CardComponent implements OnInit {
  readonly size = input.required<number>();
  readonly height = input(1);
  readonly skin = input.required<string>();
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  card!: CardEntry;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  readonly cardDiv = viewChild<any>('cardDiv');

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  readonly cardHeader = viewChild<any>('cardHeader');

  public trustedwebdavUrl!: string;

  public constructor(private loginService: LoginService) {}

  public ngOnInit() {
    if (this.card.type === 'image') {
      const cardDiv = this.cardDiv();
      if (this.card.properties) {
        cardDiv.nativeElement.style.backgroundImage = `url(${this.card.properties.img})`;
      }
      cardDiv.nativeElement.style.backgroundSize = 'cover';
    }

    if (this.card.type === 'text-image') {
      const cardHeader = this.cardHeader();
      if (this.card.properties) {
        cardHeader.nativeElement.style.backgroundImage = `url(${this.card.properties.img})`;
      }
      cardHeader.nativeElement.style.display = 'block';
    }

    if (this.card.type === 'webdav') {
      if (this.card.properties) {
        this.trustedwebdavUrl = this.card.properties.webdavUrl;
      }
    }
  }

  public isText(): boolean {
    return this.card.type === 'text' || this.card.type === 'text-image';
  }

  public isWebdav(): boolean {
    return this.card.type === 'webdav';
  }

  public isEvent(): boolean {
    return this.card.type === 'event';
  }

  public checkTicket(): string {
    if (this.loginService.isGuest()) {
      return '';
    }
    return `?ticket=${this.loginService.getTicket()}`;
  }

  public getWebdavUrl(): string {
    return this.trustedwebdavUrl + this.checkTicket();
  }
}
