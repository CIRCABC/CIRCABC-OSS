import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HelpService, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { ValidationService } from 'app/core/validation.service';
import { ContactReasons } from 'app/help/contact-support/reasons-enum';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-contact-support',
  templateUrl: './contact-support.component.html',
  styleUrls: ['./contact-support.component.scss'],
  preserveWhitespaces: true,
})
export class ContactSupportComponent implements OnInit {
  public contactForm!: FormGroup;
  public reasons: string[] = [];
  public processing = false;
  private fileToUpload: File | undefined;

  constructor(
    private fb: FormBuilder,
    private helpService: HelpService,
    private loginService: LoginService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit() {
    this.contactForm = this.fb.group({
      reason: ['', Validators.required],
      name: ['', Validators.required],
      subject: [''],
      email: ['', [Validators.required, ValidationService.emailValidator]],
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
    try {
      await firstValueFrom(
        this.helpService.contactSupport(
          this.contactForm.value.reason,
          this.contactForm.value.name,
          this.contactForm.value.email,
          this.contactForm.value.content,
          this.contactForm.value.subject,
          this.fileToUpload
        )
      );
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['/help']);
    } catch (error) {
      console.error(error);
    }
    this.processing = false;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public fileChangeEvent(fileInput: any) {
    const filesList = fileInput.target.files as FileList;
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
}
