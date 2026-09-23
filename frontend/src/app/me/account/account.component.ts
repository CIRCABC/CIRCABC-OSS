import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  effect,
  inject,
  linkedSignal,
  resource,
  signal,
  viewChild,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';

import { RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ActionEmitterResult, ActionType } from 'app/action-result';
import { AnalyticsService } from 'app/core/analytics.service';
import {
  AppMessageService,
  DistributionMail,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getSuccessTranslation } from 'app/core/util';
import { emailValidator, urlValidator } from 'app/core/validation.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { FadeInOutDirective } from 'app/shared/directives/fade-in-out.directive';
import { HintComponent } from 'app/shared/hint/hint.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { ChangeAvatarComponent } from './change-avatar/change-avatar.component';

/**
 * Account management component (`cbc-account`).
 *
 * Renders the current user's personal account page, allowing them to view and
 * edit their profile details (name, email, phone, organisation, title,
 * description, signature, postal/URL address), manage their UI language,
 * toggle notification and distribution-list subscriptions, control cookie
 * consent, change or remove their avatar, and refresh their profile from the
 * central identity database.
 *
 * The form is a reactive {@link FormGroup} (`updateUserForm`) whose values are
 * synchronised with the loaded {@link User}. The component collaborates with:
 * - {@link UserService} to load, update, and refresh the user and manage the avatar.
 * - {@link LoginService} to obtain the currently authenticated user.
 * - {@link AppMessageService} to read and manage the global distribution-mail subscription.
 * - {@link AnalyticsService} to read/apply cookie-consent state.
 * - {@link TranslocoService} for translations and switching the active UI language.
 * - {@link UiMessageService} to surface success/error feedback to the user.
 * - {@link Location} to navigate back.
 *
 * @remarks Uses OnPush change detection; form-driven and async UI state is
 * backed by signals so the view updates when it changes.
 */
@Component({
  selector: 'cbc-account',
  templateUrl: './account.component.html',
  styleUrl: './account.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    SpinnerComponent,
    ReactiveFormsModule,
    InlineDeleteComponent,
    ControlMessageComponent,
    LangSelectorComponent,
    HintComponent,
    RichTextEditorComponent,
    RouterLink,
    ChangeAvatarComponent,
    DownloadPipe,
    SecurePipe,
    TranslocoModule,
    FadeInOutDirective,
  ],
})
export class AccountComponent {
  private readonly userService = inject(UserService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly loginService = inject(LoginService);
  private readonly translateService = inject(TranslocoService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly location = inject(Location);
  private readonly appMessageService = inject(AppMessageService);
  private readonly analyticsService = inject(AnalyticsService);

  /** Reference to the first-name input element, focused after the form is populated. */
  readonly nameInputFieldElement = viewChild.required<ElementRef>(
    'nameInputFieldElement'
  );

  /**
   * Loads the current user's details and their global distribution-mail
   * subscription from the server. The distribution-mail lookup depends on
   * the loaded user's id, so both calls happen sequentially inside this
   * single loader.
   */
  private readonly userDetailsResource = resource({
    loader: async () => {
      const user = await this.userService.getUserAsync({
        userId: this.loginService.getUser().userId as string,
      });

      let distributionMail: DistributionMail | undefined;
      if (user?.userId) {
        const result =
          await this.appMessageService.getDistributionEmailSubscriptionAsync({
            userId: user.userId,
          });
        distributionMail = result?.id ? result : undefined;
      }

      return { user, distributionMail };
    },
  });

  /**
   * The user whose account is being viewed/edited. Defaults to the loaded
   * user but stays locally writable so `update()`, `cancel()`,
   * `refreshFromCentralDB()` and the avatar handlers can mutate/replace it.
   */
  public readonly user = linkedSignal<User>(
    () => this.userDetailsResource.value()?.user ?? this.loginService.getUser()
  );
  /** The user's current global distribution-mail subscription, if any. */
  public distributionMail: DistributionMail | undefined;
  /** Whether the page is in read-only viewing mode (as opposed to editing). */
  public viewing = false;
  /** Whether an asynchronous operation (save, refresh, avatar change) is in progress. */
  public readonly processing = signal(false);
  /** Whether the initial user data has been loaded and the view can be rendered. */
  public readonly ready = computed(() => this.userDetailsResource.hasValue());
  /** Whether the cookie-consent agreement alert is shown. */
  public agreeAlert = false;
  /** Whether the change-avatar dialog is open. */
  public launchChangeAvatar = false;
  /** Whether the avatar-deletion confirmation prompt must be shown. */
  public readonly mustConfirmAvatarDelete = signal(false);
  /** Reactive form backing all editable account fields. */
  public updateUserForm: FormGroup = this.formBuilder.group(
    {
      firstname: [''],
      lastname: [''],
      email: ['', emailValidator],

      title: [''],
      organisation: [''],
      postalAddress: [''],
      description: [''],
      phone: [''],
      fax: [''],
      urlAddress: ['', urlValidator],
      uiLanguage: [''],
      globalNotificationEnabled: [false],
      globalDistributionEnabled: [false],
      personalInformationVisible: [false],
      agreeWithCookies: [this.analyticsService.getAgreeWithCookies() ?? false],
      signature: [''],
      avatar: [''],
    },
    {
      updateOn: 'change',
    }
  );

  constructor() {
    // Seeds the distribution-mail subscription once the user details have
    // loaded. This runs only when the resource's value changes (not on every
    // `user()` mutation), since `subscribeToDistributionList()` /
    // `unsubscribeFromDistributionList()` manage it afterwards.
    effect(() => {
      if (this.userDetailsResource.hasValue()) {
        this.distributionMail =
          this.userDetailsResource.value().distributionMail;
      }
    });

    // Populates the form whenever the user changes (initial load, cancel,
    // refresh, avatar handlers, ...), then focuses the name input. This syncs
    // signal state into the imperative FormGroup API, so an effect is the
    // right tool here.
    effect(() => {
      if (this.userDetailsResource.hasValue()) {
        this.fillForm();
      }
    });

    this.updateUserForm.controls.globalDistributionEnabled.valueChanges.subscribe(
      (data) => {
        if (data) {
          this.subscribeToDistributionList();
        } else {
          this.unsubscribeFromDistributionList();
        }
      }
    );
  }

  /**
   * Copies the loaded {@link User} values into the reactive form controls,
   * including nested profile properties and the distribution-list toggle
   * state, then focuses the name input. Errors while parsing a server error
   * body are surfaced via the UI message service.
   */
  private fillForm() {
    try {
      const user = this.user();
      // fill form fields
      this.updateUserForm.controls.firstname.patchValue(user.firstname);
      this.updateUserForm.controls.lastname.patchValue(user.lastname);
      this.updateUserForm.controls.email.patchValue(user.email);
      this.updateUserForm.controls.phone.patchValue(user.phone);
      this.updateUserForm.controls.uiLanguage.patchValue(user.uiLang);
      this.updateUserForm.controls.personalInformationVisible.patchValue(
        user.visibility
      );
      this.updateUserForm.controls.avatar.patchValue(user.avatar);

      if (user.properties !== undefined) {
        this.updateUserForm.controls.title.patchValue(user.properties.title);
        this.updateUserForm.controls.description.patchValue(
          user.properties.description
        );
        this.updateUserForm.controls.organisation.patchValue(
          user.properties.organisation
        );
        this.updateUserForm.controls.signature.patchValue(
          user.properties.signature
        );
        this.updateUserForm.controls.fax.patchValue(user.properties.fax);
        this.updateUserForm.controls.postalAddress.patchValue(
          user.properties.postalAddress
        );
        this.updateUserForm.controls.urlAddress.patchValue(
          user.properties.urlAddress
        );
        this.updateUserForm.controls.globalNotificationEnabled.patchValue(
          user.properties.globalNotificationEnabled === 'true'
        );
      }

      if (this.distributionMail?.id) {
        this.updateUserForm.controls.globalDistributionEnabled.patchValue(true);
      } else {
        this.updateUserForm.controls.globalDistributionEnabled.patchValue(
          false
        );
      }
    } catch (error) {
      const jsonError = JSON.parse(error._body) as Record<string, string>;
      if (jsonError && 'message' in jsonError) {
        this.uiMessageService.addErrorMessage(jsonError.message);
      }
    }

    this.nameInputFieldElement().nativeElement.focus();
  }

  /**
   * Truncates a date/time string to its `YYYY-MM-DD` date portion.
   *
   * @param dateString The date string to truncate.
   * @returns The first 10 characters (date part), or an empty string when the input is undefined.
   */
  public cutDate(dateString: string) {
    return dateString === undefined ? '' : dateString.substring(0, 10);
  }

  /** Navigates back to the previous location in browser history. */
  public goBack() {
    this.location.back();
  }

  /** Opens the change-avatar dialog. */
  public changeAvatar() {
    this.launchChangeAvatar = true;
  }

  /** Closes the change-avatar dialog. */
  public changeAvatarClosed() {
    this.launchChangeAvatar = false;
  }

  /**
   * Handler invoked after a new avatar has been uploaded. Reloads the user,
   * refreshes the form, and forces a full page reload to reflect the new avatar.
   *
   * @param _result The result emitted by the avatar upload action (unused).
   * @returns A promise that resolves once the user has been reloaded.
   */
  public async avatarUploaded(_result: ActionEmitterResult) {
    this.user.set(
      await this.userService.getUserAsync({
        userId: this.user().userId as string,
      })
    );

    location.reload();
  }

  /**
   * Deletes the user's avatar, reloads the user and form, then forces a full
   * page reload so the removed avatar is reflected everywhere.
   *
   * @returns A promise that resolves once the avatar has been removed.
   */
  public async removeAvatar() {
    this.processing.set(true);

    await this.userService.deleteAvatarAsync({
      userId: this.user().userId as string,
    });

    this.user.set(
      await this.userService.getUserAsync({
        userId: this.user().userId as string,
      })
    );

    this.processing.set(false);
    this.mustConfirmAvatarDelete.set(false);

    location.reload();
  }

  /**
   * Re-fetches the user's details from the central identity database
   * (overwriting locally cached values) and repopulates the form.
   *
   * @returns A promise that resolves once the user has been refreshed.
   */
  public async refreshFromCentralDB() {
    this.processing.set(true);

    this.user.set(
      await this.userService.getUserFromDBAsync({
        userId: this.user().userId as string,
      })
    );

    this.processing.set(false);
  }

  /**
   * Discards unsaved changes by reloading the user from the server and
   * repopulating the form.
   *
   * @returns A promise that resolves once the form has been reset.
   */
  public async cancel() {
    this.user.set(
      await this.userService.getUserAsync({
        userId: this.user().userId as string,
      })
    );
  }

  /**
   * Persists the edited profile: applies cookie consent, copies form values
   * back onto the {@link User} (including nested properties), saves via
   * {@link UserService.putUser}, switches the active UI language, and shows a
   * success message. The processing flag is always cleared afterwards.
   *
   * @returns A promise that resolves once the update completes.
   * @throws {Error} If the user's `properties` are undefined (an error message is also surfaced to the UI).
   */
  public async update() {
    try {
      this.processing.set(true);
      this.setAllowCookies();
      const user = this.user();
      if (user?.properties === undefined) {
        const text = this.translateService.translate(
          getSuccessTranslation(ActionType.UPDATE_ACCOUNT)
        );
        if (text) {
          this.uiMessageService.addErrorMessage(text, true);
        }
        throw new Error('"user" is undefined.');
      }

      // fill user fields
      user.firstname = this.updateUserForm.controls.firstname.value;
      user.lastname = this.updateUserForm.controls.lastname.value;
      user.email = this.updateUserForm.controls.email.value;
      user.phone = this.updateUserForm.controls.phone.value;
      user.uiLang = this.updateUserForm.controls.uiLanguage.value;
      user.visibility =
        this.updateUserForm.controls.personalInformationVisible.value;

      user.properties.title = this.updateUserForm.controls.title.value;
      user.properties.description =
        this.updateUserForm.controls.description.value;
      user.properties.organisation =
        this.updateUserForm.controls.organisation.value;
      user.properties.signature = this.updateUserForm.controls.signature.value;
      user.properties.fax = this.updateUserForm.controls.fax.value;
      user.properties.postalAddress =
        this.updateUserForm.controls.postalAddress.value;
      user.properties.urlAddress =
        this.updateUserForm.controls.urlAddress.value;
      user.properties.globalNotificationEnabled = this.updateUserForm.controls
        .globalNotificationEnabled.value
        ? 'true'
        : 'false';

      await this.userService.putUserAsync({
        userId: user.userId as string,
        user,
      });

      this.refreshUILang(this.updateUserForm.controls.uiLanguage.value);

      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.UPDATE_ACCOUNT)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    } finally {
      this.processing.set(false);
    }
  }
  /**
   * Applies the cookie-consent choice from the form. When consent changes to
   * enabled, initialises analytics tracking.
   */
  private setAllowCookies() {
    const agreeWithCookies =
      this.analyticsService.getAgreeWithCookies() ?? false;

    if (
      agreeWithCookies !== this.updateUserForm.controls.agreeWithCookies.value
    ) {
      if (this.updateUserForm.controls.agreeWithCookies.value) {
        this.analyticsService.init();
      }
    }
  }

  /** The reactive form control for the email field. */
  get emailControl(): AbstractControl {
    return this.updateUserForm.controls.email;
  }
  /** The reactive form control for the URL address field. */
  get urlAddressControl(): AbstractControl {
    return this.updateUserForm.controls.urlAddress;
  }

  /**
   * Sets the application's active UI language.
   *
   * @param lang The language code to activate (e.g. `en`, `fr`).
   */
  private refreshUILang(lang: string) {
    this.translateService.setActiveLang(lang);
  }

  /**
   * Subscribes the user to the global distribution mailing list by registering
   * their email address, then stores the resulting {@link DistributionMail}.
   * Failures are logged and swallowed.
   *
   * @returns A promise that resolves once the subscription attempt completes.
   */
  private async subscribeToDistributionList() {
    const user = this.user();
    if (user && this.updateUserForm && user.userId) {
      try {
        const distrib: DistributionMail = {
          id: undefined,
          emailAddress: user.email,
        };
        await this.appMessageService.addDistributionEmailsAsync({
          distributionMail: [distrib],
        });

        const result: DistributionMail =
          await this.appMessageService.getDistributionEmailSubscriptionAsync({
            userId: user.userId,
          });
        if (result?.id) {
          this.distributionMail = result;
        }
      } catch (error) {
        console.error(error);
      }
    }
  }

  /**
   * Unsubscribes the user from the global distribution mailing list and clears
   * the stored subscription. Failures are logged and swallowed.
   *
   * @returns A promise that resolves once the unsubscribe attempt completes.
   */
  private async unsubscribeFromDistributionList() {
    if (this.user() && this.updateUserForm && this.distributionMail?.id) {
      try {
        await this.appMessageService.deleteDistributionEmailsAsync({
          id: this.distributionMail.id,
        });
        this.distributionMail = undefined;
      } catch (error) {
        console.error(error);
      }
    }
  }
}
