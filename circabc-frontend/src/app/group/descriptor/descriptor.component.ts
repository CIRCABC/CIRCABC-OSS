import { Component, Input, OnInit } from '@angular/core';

import { TranslocoService } from '@ngneat/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { EULoginService } from 'app/core/eulogin.service';
import { InterestGroup, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { RedirectionService } from 'app/core/redirection.service';
import { getSuccessTranslation } from 'app/core/util';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-desciptor',
  templateUrl: './descriptor.component.html',
  styleUrls: ['./descriptor.component.scss'],
  preserveWhitespaces: true,
})
export class DescriptorComponent implements OnInit {
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
    if (!this.loginService.isGuest()) {
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
            profile &&
            profile.interestGroup &&
            profile.interestGroup.id === this.group.id
          ) {
            this.alreadyMember = true;
          }
        }
      }
    } else {
      this.redirectionService.mustRedirect();
    }
  }

  public isContactLeaderAvailable(): boolean {
    return !this.loginService.isGuest();
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

  private getLogo(item: 0 | 1): string {
    if (this.group && this.group.logoUrl) {
      return this.group.logoUrl.split('/')[item];
    } else {
      return '';
    }
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
    if (this.group && this.group.description) {
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
      'https://ecas.cc.cec.eu.int:7002/cas/eim/external/register.cgi';
  }
}
