import { AfterViewInit, Component, OnInit, viewChild } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { HelpService, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { emailValidator } from 'app/core/validation.service';
import { ContactReasons } from 'app/help/contact-support/reasons-enum';
import { CaptchaComponent } from 'app/shared/captcha/captcha.component';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-contact-support',
  templateUrl: './contact-support.component.html',
  styleUrl: './contact-support.component.scss',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    ControlMessageComponent,
    EditorModule,
    SharedModule,
    CaptchaComponent,
    SpinnerComponent,
    RouterLink,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class ContactSupportComponent implements OnInit, AfterViewInit {
  public contactForm!: FormGroup;
  public reasons: string[] = [];
  public processing = false;
  public isWrongCaptcha = false;
  private fileToUpload: File | undefined;
  readonly captchaComponent = viewChild(CaptchaComponent);

  constructor(
    private fb: FormBuilder,
    private helpService: HelpService,
    private loginService: LoginService,
    private userService: UserService,
    private router: Router,
    private translateService: TranslocoService
  ) {}
  ngAfterViewInit(): void {
    const activeLang = this.translateService.getActiveLang();
    const captchaComponent = this.captchaComponent();
    if (captchaComponent !== undefined) {
      captchaComponent.answer.setValue('');
      if (activeLang !== captchaComponent.languageCode()) {
        captchaComponent.languageCode.set(activeLang);
      }
    }
  }

  ngOnInit() {
    this.contactForm = this.fb.group({
      reason: ['', Validators.required],
      name: ['', Validators.required],
      subject: [''],
      email: ['', [Validators.required, emailValidator]],
      content: ['', Validators.required],
      file: [],
    });

    Object.keys(ContactReasons).forEach((k) => {
      this.reasons.push(ContactReasons[k as keyof typeof ContactReasons]);
    });

    if (!this.loginService.isGuest()) {
      this.userService
        .getUser(this.loginService.getCurrentUsername())
        .subscribe((data) => {
          this.contactForm.controls.name.setValue(
            `${data.firstname} ${data.lastname}`
          );
          this.contactForm.controls.email.setValue(data.email);
        });
    }
    if (this.isGuest()) {
      this.translateService.langChanges$.subscribe((event: string) => {
        const captchaComponent = this.captchaComponent();
        if (captchaComponent) {
          captchaComponent.languageCode.set(event);
        }
      });
    }
  }

  get reasonControl() {
    return this.contactForm?.controls.reason;
  }

  get nameControl() {
    return this.contactForm?.controls.name;
  }

  get emailControl() {
    return this.contactForm?.controls.email;
  }

  get subjectControl() {
    return this.contactForm?.controls.subject;
  }

  get contentControl() {
    return this.contactForm?.controls.content;
  }

  get fileControl() {
    return this.contactForm?.controls.file;
  }

  public needsSubject(): boolean {
    if (this.contactForm) {
      return this.contactForm.value.reason === ContactReasons.OTHER;
    }
    return false;
  }

  public getFileName() {
    if (this.contactForm) {
      return this.contactForm.value.file;
    }

    return '';
  }

  public async contact() {
    this.processing = true;
    this.isWrongCaptcha = false;
    try {
      const captchaComponent = this.captchaComponent();
      if (captchaComponent !== undefined) {
        await firstValueFrom(
          this.helpService.contactSupport(
            this.contactForm.value.reason,
            this.contactForm.value.name,
            this.contactForm.value.email,
            this.contactForm.value.content,
            captchaComponent.captchaId,
            captchaComponent.captchaToken,
            captchaComponent.answer.value as string,
            this.contactForm.value.subject,
            this.fileToUpload
          )
        );
      } else {
        await firstValueFrom(
          this.helpService.contactSupport(
            this.contactForm.value.reason,
            this.contactForm.value.name,
            this.contactForm.value.email,
            this.contactForm.value.content,
            undefined,
            undefined,
            undefined,
            this.contactForm.value.subject,
            this.fileToUpload
          )
        );
      }
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['/help']);
    } catch (error) {
      if (error.error?.message?.includes('invalid captcha answer')) {
        this.isWrongCaptcha = true;
        this.processing = false;
      }
    }
    this.processing = false;
  }

  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.handleFiles(filesList);
  }

  private handleFiles(filesList: FileList) {
    this.fileToUpload = undefined;

    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.fileToUpload = fileItem;
      }
    }
  }

  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  public isDisabled(): boolean {
    const component = this.captchaComponent();
    if (this.isGuest() && component) {
      return !this.contactForm.valid || component.answer.invalid;
    }
    return !this.contactForm.valid;
  }
}
