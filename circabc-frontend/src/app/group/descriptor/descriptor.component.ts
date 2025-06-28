import { Component, Input, OnInit } from '@angular/core';

import { I18nSelectPipe } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { EULoginService } from 'app/core/eulogin.service';
import { type InterestGroup, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { RedirectionService } from 'app/core/redirection.service';
import { getSuccessTranslation } from 'app/core/util';
import { LeaderContactComponent } from 'app/group/leader-contact/leader-contact.component';
import { MembershipApplicationComponent } from 'app/group/membership-application/membership-application.component';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { ShareComponent } from 'app/shared/share/share.component';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-desciptor',
  templateUrl: './descriptor.component.html',
  styleUrl: './descriptor.component.scss',
  preserveWhitespaces: true,
  imports: [
    MatMenuModule,
    RouterLink,
    ShareComponent,
    MembershipApplicationComponent,
    LeaderContactComponent,
    I18nSelectPipe,
    DownloadPipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class DescriptorComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  public group!: InterestGroup;

  public showApplicationModal = false;
  public showContactLeadersModal = false;
  public alreadyMember = false;

  constructor(
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private loginService: LoginService,
    private userService: UserService,
    private euLoginService: EULoginService,
    private redirectionService: RedirectionService
  ) {}

  async ngOnInit() {
    if (this.loginService.isGuest()) {
      this.redirectionService.mustRedirect();
    } else {
      const userId =
        this.loginService.getUser().userId !== undefined
          ? this.loginService.getUser().userId
          : 'guest';
      if (userId !== undefined) {
        const memberships = await firstValueFrom(
          this.userService.getUserMembership(userId)
        );
        for (const profile of memberships) {
          if (
            profile?.interestGroup &&
            profile.interestGroup.id === this.group.id
          ) {
            this.alreadyMember = true;
          }
        }
      }
    }
  }

  public isContactLeaderAvailable(): boolean {
    return !this.loginService.isGuest();
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

  private getLogo(item: 0 | 1): string {
    if (this.group?.logoUrl) {
      return this.group.logoUrl.split('/')[item];
    }
    return '';
  }

  getLogoRef(): string {
    return this.getLogo(0);
  }

  getLogoName(): string {
    return this.getLogo(1);
  }

  isJoinEnabled() {
    return (
      this.group.allowApply &&
      !this.loginService.isGuest() &&
      !this.alreadyMember
    );
  }

  isJoinEnabledGuest() {
    return this.group.allowApply && this.loginService.isGuest();
  }

  onRequestCanceled(_result: ActionEmitterResult) {
    this.showApplicationModal = false;
  }

  async onRequestFinished(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.APPLY_FOR_MEMBERSHIP
    ) {
      this.showApplicationModal = false;
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.APPLY_FOR_MEMBERSHIP)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }
  }

  hasDescription(): boolean {
    if (this.group?.description) {
      return this.hasMLValue(this.group.description);
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

  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  public leaderContactRefresh(_result: ActionEmitterResult) {
    this.showContactLeadersModal = false;
  }

  public get useEULogin(): boolean {
    return environment.circabcRelease !== 'oss';
  }

  public euLogin() {
    this.euLoginService.euLogin();
  }

  public euLoginCreate() {
    window.location.href =
      '';
  }
}
