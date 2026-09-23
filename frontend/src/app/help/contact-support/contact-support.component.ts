import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
  viewChild,
} from '@angular/core';
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
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone Angular component that renders the "Contact support" page.
 *
 * It displays a reactive form allowing a user (authenticated or guest) to
 * reach out to the support team. The form collects a reason, the sender's
 * name and email, an optional subject, a rich-text message body and an
 * optional file attachment. Guests must additionally solve an EU CAPTCHA
 * challenge before the request is accepted.
 *
 * On submit the data is sent through {@link HelpService.contactSupport};
 * on success the user is navigated to the `/help` page. When the backend
 * reports an invalid CAPTCHA answer, an error flag is raised so the
 * template can prompt the user to try again.
 *
 * Key collaborators:
 * - {@link HelpService} — sends the support request to the backend.
 * - {@link UserService} — pre-fills name/email for authenticated users.
 * - {@link LoginService} — determines whether the current user is a guest.
 * - {@link TranslocoService} — provides the active language, also used to
 *   keep the embedded {@link CaptchaComponent} in sync with language changes.
 * - {@link Router} — navigates away after a successful submission.
 */
@Component({
  selector: 'cbc-contact-support',
  templateUrl: './contact-support.component.html',
  styleUrl: './contact-support.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    ControlMessageComponent,
    RichTextEditorComponent,
    CaptchaComponent,
    SpinnerComponent,
    RouterLink,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class ContactSupportComponent implements OnInit, AfterViewInit {
  /** Factory used to build the reactive contact form. */
  private readonly fb = inject(FormBuilder);
  /** Backend service used to submit the support request. */
  private readonly helpService = inject(HelpService);
  /** Provides the current authentication state and username. */
  private readonly loginService = inject(LoginService);
  /** Used to fetch the current user's details to pre-fill the form. */
  private readonly userService = inject(UserService);
  /** Router used to navigate to `/help` after a successful submission. */
  private readonly router = inject(Router);
  /** Provides the active language and language-change notifications. */
  private readonly translateService = inject(TranslocoService);

  /** Reactive form holding the reason, name, subject, email, content and file controls. */
  public contactForm!: FormGroup;
  /** List of selectable contact reasons derived from {@link ContactReasons}. */
  public reasons: string[] = [];
  /** Whether a submission is currently in progress (drives the spinner/disabled state). */
  public readonly processing = signal(false);
  /** True when the last submission failed because of an invalid CAPTCHA answer. */
  public readonly isWrongCaptcha = signal(false);
  /** The file selected by the user to attach to the support request, if any. */
  private fileToUpload: File | undefined;
  /** Query for the embedded CAPTCHA component (present only for guest users). */
  readonly captchaComponent = viewChild(CaptchaComponent);

  /**
   * Angular lifecycle hook run after the view has been initialised.
   *
   * Resets the CAPTCHA answer and ensures the CAPTCHA component's language
   * code matches the currently active Transloco language.
   */
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

  /**
   * Angular lifecycle hook run on initialisation.
   *
   * Builds the contact form with its validators, populates the list of
   * available reasons, pre-fills the name and email fields for
   * authenticated users, and — for guests — subscribes to language changes
   * so the CAPTCHA component stays localised.
   */
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
        .getUser({ userId: this.loginService.getCurrentUsername() })
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

  /** @returns The `reason` form control, or undefined if the form is not yet built. */
  get reasonControl() {
    return this.contactForm?.controls.reason;
  }

  /** @returns The `name` form control, or undefined if the form is not yet built. */
  get nameControl() {
    return this.contactForm?.controls.name;
  }

  /** @returns The `email` form control, or undefined if the form is not yet built. */
  get emailControl() {
    return this.contactForm?.controls.email;
  }

  /** @returns The `subject` form control, or undefined if the form is not yet built. */
  get subjectControl() {
    return this.contactForm?.controls.subject;
  }

  /** @returns The `content` form control, or undefined if the form is not yet built. */
  get contentControl() {
    return this.contactForm?.controls.content;
  }

  /** @returns The `file` form control, or undefined if the form is not yet built. */
  get fileControl() {
    return this.contactForm?.controls.file;
  }

  /**
   * Indicates whether a subject must be provided for the selected reason.
   *
   * @returns `true` when the chosen reason is {@link ContactReasons.OTHER},
   * otherwise `false`.
   */
  public needsSubject(): boolean {
    if (this.contactForm) {
      return this.contactForm.value.reason === ContactReasons.OTHER;
    }
    return false;
  }

  /**
   * Returns the current value of the `file` form control.
   *
   * @returns The file control value, or an empty string when the form is
   * not yet built.
   */
  public getFileName() {
    if (this.contactForm) {
      return this.contactForm.value.file;
    }

    return '';
  }

  /**
   * Submits the contact form to the support backend.
   *
   * Builds and sends a {@link HelpService.contactSupport} request. When a
   * CAPTCHA component is present (guest users) its id, token and answer are
   * included; otherwise the request is sent without CAPTCHA data. On success
   * the user is redirected to `/help`. If the backend responds with an
   * "invalid captcha answer" message, {@link isWrongCaptcha} is set so the
   * template can inform the user.
   *
   * @returns A promise that resolves once the submission attempt completes.
   */
  public async contact() {
    this.processing.set(true);
    this.isWrongCaptcha.set(false);
    try {
      const captchaComponent = this.captchaComponent();
      if (captchaComponent === undefined) {
        await this.helpService.contactSupportAsync({
          reason: this.contactForm.value.reason,
          name: this.contactForm.value.name,
          email: this.contactForm.value.email,
          content: this.contactForm.value.content,
          subject: this.contactForm.value.subject,
          fileData1: this.fileToUpload,
        });
      } else {
        await this.helpService.contactSupportAsync({
          reason: this.contactForm.value.reason,
          name: this.contactForm.value.name,
          email: this.contactForm.value.email,
          content: this.contactForm.value.content,
          xEUCAPTCHAID: captchaComponent.captchaId,
          xEUCAPTCHATOKEN: captchaComponent.captchaToken,
          xEUCAPTCHATEXT: captchaComponent.answer.value as string,
          subject: this.contactForm.value.subject,
          fileData1: this.fileToUpload,
        });
      }

      this.router.navigate(['/help']);
    } catch (error) {
      if (error.error?.message?.includes('invalid captcha answer')) {
        this.isWrongCaptcha.set(true);
        this.processing.set(false);
      }
    }
    this.processing.set(false);
  }

  /**
   * Handles the file input's change event and records the selected file.
   *
   * @param event The DOM change event emitted by the file `<input>`.
   */
  public fileChangeEvent(event: Event) {
    const input = event.target as HTMLInputElement;
    const filesList = input.files as FileList;

    this.handleFiles(filesList);
  }

  /**
   * Stores the last file from the provided list as the attachment to upload.
   *
   * @param filesList The list of files selected through the file input.
   */
  private handleFiles(filesList: FileList) {
    this.fileToUpload = undefined;

    for (let i = 0; i < filesList.length; i += 1) {
      const fileItem = filesList.item(i);
      if (fileItem) {
        this.fileToUpload = fileItem;
      }
    }
  }

  /**
   * Indicates whether the current user is a guest (not authenticated).
   *
   * @returns `true` when the current user is a guest, otherwise `false`.
   */
  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  /**
   * Determines whether the submit action should be disabled.
   *
   * For guests with a CAPTCHA present, the form must be valid and the
   * CAPTCHA answer must be valid; for other users only the form validity is
   * considered.
   *
   * @returns `true` when submission should be disabled, otherwise `false`.
   */
  public isDisabled(): boolean {
    const component = this.captchaComponent();
    if (this.isGuest() && component) {
      return !this.contactForm.valid || component.answer.invalid;
    }
    return !this.contactForm.valid;
  }
}
