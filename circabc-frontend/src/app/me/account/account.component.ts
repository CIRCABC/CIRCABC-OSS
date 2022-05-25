import { Location } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup } from '@angular/forms';

import { TranslocoService } from '@ngneat/transloco';
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
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'cbc-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss'],
  preserveWhitespaces: true,
})
export class AccountComponent implements OnInit {
  @ViewChild('nameInputFieldElement', { static: true })
  nameInputFieldElement!: ElementRef;

  public user!: User;
  public distributionMail!: DistributionMail | undefined;

  public viewing = false;
  public processing = false;
  public ready = false;

  public launchChangeAvatar = false;
  public mustConfirmAvatarDelete = false;

  public updateUserForm!: FormGroup;

  constructor(
    private userService: UserService,
    private formBuilder: FormBuilder,
    private loginService: LoginService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private location: Location,
    private appMessageService: AppMessageService,
    private analyticsService: AnalyticsService,
    private dialog: MatDialog
  ) {}

  async ngOnInit() {
    const agreeWithCookies =
      this.analyticsService.getAgreeWithCookies() ?? false;
    this.updateUserForm = this.formBuilder.group(
      {
        firstname: [''],
        lastname: [''],
        email: ['', ValidationService.emailValidator],

        title: [''],
        organisation: [''],
        postalAddress: [''],
        description: [''],
        phone: [''],
        fax: [''],
        urlAddress: ['', ValidationService.urlValidator],
        uiLanguage: [''],
        globalNotificationEnabled: [false],
        globalDistributionEnabled: [false],
        personalInformationVisible: [false],
        agreeWithCookies: [agreeWithCookies],
        signature: [''],
        avatar: [''],
      },
      {
        updateOn: 'change',
      }
    );

    if (this.user === undefined) {
      this.user = this.loginService.getUser();
    }

    await this.loadUserDetails();
    this.ready = true;
    await this.fillForm();

    this.updateUserForm.controls.globalDistributionEnabled.valueChanges.subscribe(
      (data) => {
        this.subscribeDistributionList(data);
      }
    );

    this.showAlertDialog();
  }

  private async showAlertDialog() {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        message: 'label.dialog.alert.updating.user.line1',
        message2: 'label.dialog.alert.updating.user.line2',
        layoutStyle: 'alert',
      },
    });
  }

  private async fillForm() {
    try {
      // fill form fields
      this.updateUserForm.controls.firstname.patchValue(this.user.firstname);
      this.updateUserForm.controls.lastname.patchValue(this.user.lastname);
      this.updateUserForm.controls.email.patchValue(this.user.email);
      this.updateUserForm.controls.phone.patchValue(this.user.phone);
      this.updateUserForm.controls.uiLanguage.patchValue(this.user.uiLang);
      this.updateUserForm.controls.personalInformationVisible.patchValue(
        this.user.visibility
      );
      this.updateUserForm.controls.avatar.patchValue(this.user.avatar);

      if (this.user.properties !== undefined) {
        this.updateUserForm.controls.title.patchValue(
          this.user.properties.title
        );
        this.updateUserForm.controls.description.patchValue(
          this.user.properties.description
        );
        this.updateUserForm.controls.organisation.patchValue(
          this.user.properties.organisation
        );
        this.updateUserForm.controls.signature.patchValue(
          this.user.properties.signature
        );
        this.updateUserForm.controls.fax.patchValue(this.user.properties.fax);
        this.updateUserForm.controls.postalAddress.patchValue(
          this.user.properties.postalAddress
        );
        this.updateUserForm.controls.urlAddress.patchValue(
          this.user.properties.urlAddress
        );
        this.updateUserForm.controls.globalNotificationEnabled.patchValue(
          this.user.properties.globalNotificationEnabled === 'true'
        );
      }

      if (this.distributionMail && this.distributionMail.id) {
        this.updateUserForm.controls.globalDistributionEnabled.patchValue(true);
      } else {
        this.updateUserForm.controls.globalDistributionEnabled.patchValue(
          false
        );
      }
    } catch (error) {
      const jsonError = JSON.parse(error._body);
      if (jsonError) {
        this.uiMessageService.addErrorMessage(jsonError.message);
      }
    }

    this.nameInputFieldElement.nativeElement.focus();
  }

  public cutDate(dateString: string) {
    return dateString !== undefined ? dateString.substring(0, 10) : '';
  }

  public goBack() {
    this.location.back();
  }

  public changeAvatar() {
    this.launchChangeAvatar = true;
  }

  public changeAvatarClosed() {
    this.launchChangeAvatar = false;
  }

  public async avatarUploaded(_result: ActionEmitterResult) {
    this.user = await firstValueFrom(
      this.userService.getUser(this.user.userId as string)
    );
    await this.fillForm();
    // eslint-disable-next-line
    location.reload();
  }

  public async removeAvatar() {
    this.processing = true;

    await firstValueFrom(
      this.userService.deleteAvatar(this.user.userId as string)
    );

    this.user = await firstValueFrom(
      this.userService.getUser(this.user.userId as string)
    );
    await this.fillForm();

    this.processing = false;
    this.mustConfirmAvatarDelete = false;
    // eslint-disable-next-line
    location.reload();
  }

  public async refreshFromCentralDB() {
    this.processing = true;

    this.user = await firstValueFrom(
      this.userService.getUserFromDB(this.user.userId as string)
    );
    await this.fillForm();

    this.processing = false;
  }

  public async cancel() {
    this.user = await firstValueFrom(
      this.userService.getUser(this.user.userId as string)
    );
    await this.fillForm();
  }

  public async update() {
    try {
      this.processing = true;
      this.setAllowCookies();
      if (this.user !== undefined && this.user.properties !== undefined) {
        // fill user fields
        this.user.firstname = this.updateUserForm.controls.firstname.value;
        this.user.lastname = this.updateUserForm.controls.lastname.value;
        this.user.email = this.updateUserForm.controls.email.value;
        this.user.phone = this.updateUserForm.controls.phone.value;
        this.user.uiLang = this.updateUserForm.controls.uiLanguage.value;
        this.user.visibility =
          this.updateUserForm.controls.personalInformationVisible.value;

        this.user.properties.title = this.updateUserForm.controls.title.value;
        this.user.properties.description =
          this.updateUserForm.controls.description.value;
        this.user.properties.organisation =
          this.updateUserForm.controls.organisation.value;
        this.user.properties.signature =
          this.updateUserForm.controls.signature.value;
        this.user.properties.fax = this.updateUserForm.controls.fax.value;
        this.user.properties.postalAddress =
          this.updateUserForm.controls.postalAddress.value;
        this.user.properties.urlAddress =
          this.updateUserForm.controls.urlAddress.value;
        this.user.properties.globalNotificationEnabled = this.updateUserForm
          .controls.globalNotificationEnabled.value
          ? 'true'
          : 'false';

        await firstValueFrom(
          this.userService.putUser(this.user.userId as string, this.user)
        );

        await this.refreshUILang(this.updateUserForm.controls.uiLanguage.value);

        const text = this.translateService.translate(
          getSuccessTranslation(ActionType.UPDATE_ACCOUNT)
        );
        if (text) {
          this.uiMessageService.addSuccessMessage(text, true);
        }
      } else {
        const text = this.translateService.translate(
          getSuccessTranslation(ActionType.UPDATE_ACCOUNT)
        );
        if (text) {
          this.uiMessageService.addErrorMessage(text, true);
        }
        throw new Error('"user" is undefined.');
      }
    } finally {
      this.processing = false;
    }
  }
  private setAllowCookies() {
    const agreeWithCookies =
      this.analyticsService.getAgreeWithCookies() ?? false;

    if (
      agreeWithCookies !== this.updateUserForm.controls.agreeWithCookies.value
    ) {
      this.analyticsService.setAgreeWithCookies(
        this.updateUserForm.controls.agreeWithCookies.value
      );
      if (this.updateUserForm.controls.agreeWithCookies.value) {
        this.analyticsService.init();
      }
    }
  }

  get emailControl(): AbstractControl {
    return this.updateUserForm.controls.email;
  }
  get urlAddressControl(): AbstractControl {
    return this.updateUserForm.controls.urlAddress;
  }

  public async refreshUILang(lang: string) {
    this.translateService.setActiveLang(lang);
    if (!this.loginService.isGuest()) {
      await firstValueFrom(
        this.userService.putUser(this.loginService.getCurrentUsername(), {
          uiLang: lang,
        })
      );
    }
  }

  private async subscribeDistributionList(value: boolean) {
    if (
      this.user &&
      this.updateUserForm &&
      value === true &&
      this.user.userId
    ) {
      try {
        const distrib: DistributionMail = {
          id: undefined,
          emailAddress: this.user.email,
        };
        await firstValueFrom(
          this.appMessageService.addDistributionEmails([distrib])
        );

        const result = await firstValueFrom(
          this.appMessageService.getDistributionEmailSubscription(
            this.user.userId
          )
        );
        if (result && result.id) {
          this.distributionMail = result;
        }
      } catch (error) {
        console.error(error);
      }
    } else if (
      this.user &&
      this.updateUserForm &&
      value === false &&
      this.distributionMail &&
      this.distributionMail.id
    ) {
      try {
        await firstValueFrom(
          this.appMessageService.deleteDistributionEmails(
            this.distributionMail.id
          )
        );
        this.distributionMail = undefined;
      } catch (error) {
        console.error(error);
      }
    }
  }

  private async loadUserDetails() {
    this.user = await firstValueFrom(
      this.userService.getUser(this.user.userId as string)
    );

    if (this.user && this.user.userId) {
      const result = await firstValueFrom(
        this.appMessageService.getDistributionEmailSubscription(
          this.user.userId
        )
      );
      if (result && result.id) {
        this.distributionMail = result;
      }
    }
  }
}
