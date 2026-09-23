import { I18nSelectPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  resource,
} from '@angular/core';
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

/**
 * Presentational component that renders the descriptor (header) of an
 * interest group: its logo, localized title/description, and the primary
 * membership actions available to the current user (join/apply, contact
 * leaders, share). It adapts its behavior based on authentication state,
 * exposing EU Login entry points when the current CIRCABC release is not the
 * open-source (`oss`) build.
 *
 * On initialization it determines whether the current (non-guest) user is
 * already a member of the displayed group so that the join action can be
 * hidden accordingly. Guests are redirected via the {@link RedirectionService}.
 *
 * Key collaborators: {@link LoginService} (auth state), {@link UserService}
 * (membership lookup), {@link EULoginService} (EU Login flow),
 * {@link RedirectionService} (guest redirection), {@link UiMessageService}
 * (success notifications) and {@link TranslocoService} (localization).
 */
@Component({
  selector: 'cbc-group-desciptor',
  templateUrl: './descriptor.component.html',
  styleUrl: './descriptor.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
export class DescriptorComponent {
  private readonly translateService = inject(TranslocoService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly loginService = inject(LoginService);
  private readonly userService = inject(UserService);
  private readonly euLoginService = inject(EULoginService);
  private readonly redirectionService = inject(RedirectionService);

  /**
   * Required input holding the interest group whose descriptor is rendered.
   * All display logic (logo, localized description, join availability) derives
   * from this value.
   */
  public group = input.required<InterestGroup>();

  /** Whether the membership application modal is currently visible. */
  public showApplicationModal = false;
  /** Whether the "contact leaders" modal is currently visible. */
  public showContactLeadersModal = false;

  /**
   * Redirects guests via the redirection service. This is a genuine
   * imperative side effect (not data to be stored in state), so it is kept
   * as an `effect()` rather than folded into {@link membershipResource}.
   */
  constructor() {
    effect(() => {
      if (this.loginService.isGuest()) {
        this.redirectionService.mustRedirect();
      }
    });
  }

  /**
   * Loads the current (non-guest) user's memberships so that
   * {@link alreadyMember} can be derived. Idle while the user is a guest,
   * since guests are redirected instead (see the constructor's `effect`).
   */
  private readonly membershipResource = resource({
    params: () => (this.loginService.isGuest() ? undefined : true),
    loader: async () => {
      const userId =
        this.loginService.getUser().userId === undefined
          ? 'guest'
          : this.loginService.getUser().userId;
      if (userId === undefined) {
        return [];
      }
      return await this.userService.getUserMembershipAsync({ userId });
    },
  });

  /**
   * Whether the current user is already a member of {@link group}. Derived
   * from {@link membershipResource} and used to hide the join action for
   * existing members.
   */
  public alreadyMember = computed(() => {
    const memberships = this.membershipResource.hasValue()
      ? this.membershipResource.value()
      : undefined;
    return (
      memberships?.some(
        (profile) => profile?.interestGroup?.id === this.group().id
      ) ?? false
    );
  });

  /**
   * Indicates whether the "contact leader" action should be offered.
   *
   * @returns `true` for authenticated users, `false` for guests.
   */
  public isContactLeaderAvailable(): boolean {
    return !this.loginService.isGuest();
  }

  /**
   * Resolves the language key to use when reading the group's localized
   * description.
   *
   * @returns The active Transloco language if the description provides a value
   * for it, otherwise the default language.
   */
  getLang(): string {
    const description = this.group().description;
    if (
      description &&
      Object.keys(description).includes(this.translateService.getActiveLang())
    ) {
      return this.translateService.getActiveLang();
    }
    return this.translateService.getDefaultLang();
  }

  /**
   * Extracts one segment of the group's `logoUrl`, which is expected to be a
   * slash-separated `reference/name` string.
   *
   * @param item The segment index to return: `0` for the reference, `1` for
   * the file name.
   * @returns The requested URL segment, or an empty string when no logo is set.
   */
  private getLogo(item: 0 | 1): string {
    const logoUrl = this.group()?.logoUrl;
    if (logoUrl) {
      return logoUrl.split('/')[item];
    }
    return '';
  }

  /**
   * @returns The reference (first) segment of the group's logo URL.
   */
  getLogoRef(): string {
    return this.getLogo(0);
  }

  /**
   * @returns The name (second) segment of the group's logo URL.
   */
  getLogoName(): string {
    return this.getLogo(1);
  }

  /**
   * Determines whether the inline "join/apply" action should be shown to an
   * authenticated user.
   *
   * @returns `true` when the group allows applications and the current
   * non-guest user is not already a member.
   */
  isJoinEnabled() {
    return (
      this.group().allowApply &&
      !this.loginService.isGuest() &&
      !this.alreadyMember()
    );
  }

  /**
   * Determines whether the guest-oriented "join/apply" action should be shown.
   *
   * @returns `true` when the group allows applications and the current user is
   * a guest.
   */
  isJoinEnabledGuest() {
    return this.group().allowApply && this.loginService.isGuest();
  }

  /**
   * Handles cancellation of the membership application flow by closing the
   * application modal.
   *
   * @param _result The emitted action result (unused).
   */
  onRequestCanceled(_result: ActionEmitterResult) {
    this.showApplicationModal = false;
  }

  /**
   * Handles completion of the membership application flow. On a successful
   * apply-for-membership action it closes the modal and displays a localized
   * success message.
   *
   * @param result The emitted action result describing the outcome and type.
   * @returns A promise that resolves once the result has been processed.
   */
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

  /**
   * Determines whether the group has a non-empty description in the resolved
   * display language.
   *
   * @returns `true` when a localized description value is available.
   */
  hasDescription(): boolean {
    const description = this.group().description;
    if (description) {
      return this.hasMLValue(description);
    }
    return false;
  }

  /**
   * Checks whether a multilingual value map contains a non-empty entry for the
   * resolved display language.
   *
   * @param obj A map of language keys to localized string values.
   * @returns `true` when the map holds a defined, non-empty value for the
   * current display language.
   */
  hasMLValue(obj: { [key: string]: string }): boolean {
    if (obj) {
      const lang = this.getLang();
      if (obj[lang] !== undefined && obj[lang] !== '') {
        return true;
      }
    }

    return false;
  }

  /**
   * @returns `true` when the current user is an unauthenticated guest.
   */
  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  /**
   * Handles completion of the "contact leaders" flow by closing its modal.
   *
   * @param _result The emitted action result (unused).
   */
  public leaderContactRefresh(_result: ActionEmitterResult) {
    this.showContactLeadersModal = false;
  }

  /**
   * Indicates whether EU Login should be used for authentication.
   *
   * @returns `true` for every CIRCABC release other than the open-source
   * (`oss`) build.
   */
  public get useEULogin(): boolean {
    return environment.circabcRelease !== 'oss';
  }

  /**
   * Initiates the EU Login authentication flow via the EU Login service.
   */
  public euLogin() {
    this.euLoginService.euLogin();
  }

  /**
   * Redirects the browser to the external EU Login (ECAS) account
   * registration page.
   */
  public euLoginCreate() {
    globalThis.location.href =
      'https://ecas.cc.cec.eu.int:7002/cas/eim/external/register.cgi';
  }
}
