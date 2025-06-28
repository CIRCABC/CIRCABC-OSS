import { Component, OnInit, signal, computed } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { TranslocoModule } from '@jsverse/transloco';
import { CaptchaControllerService } from 'app/core/generated/eu-captcha';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-captcha',
  templateUrl: './captcha.component.html',
  styleUrl: './captcha.component.scss',
  imports: [ReactiveFormsModule, ControlMessageComponent, TranslocoModule],
})
export class CaptchaComponent implements OnInit {
  constructor(
    private captchaService: CaptchaControllerService,
    private sanitizer: DomSanitizer
  ) {}

  private capitalizationValue = false;
  private sizeValue = 8;

  languageCode = signal('en');

  captchaLanguageId = computed(() => {
    const result = this.captchaLang.find(
      (captchaLang) => captchaLang.code === this.languageCode()
    );
    let newCaptchaLanguageId = 'en-GB';
    if (result) {
      newCaptchaLanguageId = result.id;
    }
    return newCaptchaLanguageId;
  });
  captchaImage!: string;
  captchaId!: string;
  captchaToken!: string;
  audioCaptcha!: SafeResourceUrl;
  answer = new FormControl('', [
    Validators.required,
    Validators.maxLength(this.sizeValue),
  ]);
  private captchaLang = [
    { code: 'en', id: 'en-GB', name: 'English' },
    { code: 'fr', id: 'fr-FR', name: 'Français' },
    { code: 'de', id: 'de-DE', name: 'Deutsch' },
    { code: 'bg', id: 'bg-BG', name: 'български' },
    { code: 'hr', id: 'hr-HR', name: 'Hrvatski' },
    { code: 'da', id: 'da-DA', name: 'Dansk' },
    { code: 'es', id: 'es-ES', name: 'Español' },
    { code: 'et', id: 'et-ET', name: 'Eesti keel' },
    { code: 'fi', id: 'fi-FI', name: 'Suomi' },
    { code: 'el', id: 'el-EL', name: 'ελληνικά' },
    { code: 'hu', id: 'hu-HU', name: 'Magyar' },
    { code: 'it', id: 'it-IT', name: 'Italiano' },
    { code: 'lv', id: 'lv-LV', name: 'Latviešu valoda' },
    { code: 'lt', id: 'lt-LT', name: 'Lietuvių kalba' },
    { code: 'mt', id: 'mt-MT', name: 'Malti' },
    { code: 'nl', id: 'nl-NL', name: 'Nederlands' },
    { code: 'pl', id: 'pl-PL', name: 'Polski' },
    { code: 'pt', id: 'pt-PT', name: 'Português' },
    { code: 'ro', id: 'ro-RO', name: 'Română' },
    { code: 'sk', id: 'sk-SK', name: 'Slovenčina' },
    { code: 'sl', id: 'sl-SL', name: 'Slovenščina' },
    { code: 'sv', id: 'sv-SV', name: 'Svenska' },
    { code: 'cs', id: 'cs-CS', name: 'čeština' },
  ];

  async ngOnInit() {
    await this.init();
  }

  private async init() {
    const captcha = await firstValueFrom(
      this.captchaService.getCaptchaImageUsingGET(
        this.captchaLanguageId(),
        this.sizeValue,
        undefined,
        this.capitalizationValue,
        undefined,
        undefined,
        'response'
      )
    );

    if (captcha.body !== null) {
      this.captchaImage = `data:image/png;base64,${captcha.body.captchaImg}`;
      this.captchaId = captcha.body.captchaId;
      this.audioCaptcha = this.sanitizer.bypassSecurityTrustResourceUrl(
        `data:audio/wav;base64,${captcha.body.audioCaptcha}`
      );
    }

    this.captchaToken = captcha.headers.get('x-jwtString') as string;
  }
  public async refresh() {
    await this.init();
  }
}
