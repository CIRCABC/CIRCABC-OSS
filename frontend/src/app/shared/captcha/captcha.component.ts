import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  resource,
  signal,
  untracked,
} from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { TranslocoModule } from '@jsverse/transloco';
import { CaptchaControllerService } from 'app/core/generated/eu-captcha';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { firstValueFrom } from 'rxjs';

/** Successful outcome of fetching a CAPTCHA challenge. */
interface CaptchaChallenge {
  /** Base64 `data:image/png` URL of the CAPTCHA image. */
  image: string;
  /** Identifier of the CAPTCHA challenge, submitted with the answer. */
  id: string;
  /** JWT token returned in the `x-jwtString` header. */
  token: string;
  /** Trusted `data:audio/wav` URL for the accessible audio version. */
  audio: SafeResourceUrl;
}

/**
 * Standalone Angular component (`cbc-captcha`) that renders an EU CAPTCHA
 * challenge for use inside reactive forms.
 *
 * The component fetches a CAPTCHA image (and an accompanying audio version for
 * accessibility) from the EU CAPTCHA backend, displays it together with a text
 * input for the user's answer, and exposes loading/error states so the template
 * can show spinners or error messages. It also supports refreshing the
 * challenge and automatically reloads it whenever the active language changes.
 *
 * The answer input, the returned CAPTCHA id and the JWT token are read by the
 * hosting form/component to submit and validate the CAPTCHA on the server side.
 *
 * Key collaborators:
 * - {@link CaptchaControllerService} — generated EU CAPTCHA API client used to
 *   retrieve the challenge.
 * - {@link DomSanitizer} — used to mark the base64 audio data URL as a trusted
 *   resource URL.
 */
@Component({
  selector: 'cbc-captcha',
  templateUrl: './captcha.component.html',
  styleUrl: './captcha.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, ControlMessageComponent, TranslocoModule],
})
export class CaptchaComponent {
  /** Generated EU CAPTCHA API client used to fetch the CAPTCHA challenge. */
  private readonly captchaService = inject(CaptchaControllerService);
  /** Sanitizer used to trust the base64-encoded audio CAPTCHA data URL. */
  private readonly sanitizer = inject(DomSanitizer);

  /** Whether the CAPTCHA answer is case-sensitive; requested from the backend. */
  private readonly capitalizationValue = false;
  /** Number of characters in the CAPTCHA and the max length of the answer input. */
  private readonly sizeValue = 8;

  /**
   * Reactive signal holding the current UI language code (e.g. `'en'`).
   * Updating it triggers a reload of the CAPTCHA in the matching language.
   */
  languageCode = signal('en');

  /**
   * Computed signal that maps the current {@link languageCode} to the locale
   * identifier expected by the EU CAPTCHA service (e.g. `'en'` -> `'en-GB'`),
   * falling back to `'en-GB'` when the code is not supported.
   */
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

  /**
   * Resource that fetches the CAPTCHA challenge for the current
   * {@link captchaLanguageId}. Re-runs the loader whenever the language
   * changes. Failures are caught inside the loader (surfaced via
   * {@link hasError}) so the resource never enters the `error` state.
   */
  private readonly captchaResource = resource({
    params: () => this.captchaLanguageId(),
    loader: async ({ params: captchaLanguageId }) => {
      try {
        const captcha = await firstValueFrom(
          this.captchaService.getCaptchaImageUsingGET(
            captchaLanguageId,
            this.sizeValue,
            undefined,
            this.capitalizationValue,
            undefined,
            undefined,
            'response'
          )
        );

        if (captcha.body === null) {
          return undefined;
        }

        return {
          image: `data:image/png;base64,${captcha.body.captchaImg}`,
          id: captcha.body.captchaId,
          token: captcha.headers.get('x-jwtString') as string,
          // prettier-ignore
          audio: this.sanitizer.bypassSecurityTrustResourceUrl(`data:audio/wav;base64,${captcha.body.audioCaptcha}`), // NOSONAR Safe - audio data comes from backend captcha service as base64 encoded WAV
        } satisfies CaptchaChallenge;
      } catch (error) {
        console.error('Failed to load captcha:', error);
        return undefined;
      }
    },
  });

  /** Base64 `data:image/png` URL of the current CAPTCHA image. */
  captchaImage = computed(() => this.captchaResource.value()?.image ?? '');
  /**
   * Identifier of the current CAPTCHA challenge, submitted with the answer.
   *
   * Kept as a plain (non-signal) field — read directly (without calling it)
   * by the hosting component via `viewChild` — and synced from
   * {@link captchaResource} in the constructor.
   */
  captchaId!: string;
  /**
   * JWT token returned in the `x-jwtString` header, used for server-side
   * validation.
   *
   * Kept as a plain (non-signal) field for the same reason as
   * {@link captchaId}.
   */
  captchaToken!: string;
  /** Trusted `data:audio/wav` URL for the accessible audio version of the CAPTCHA. */
  audioCaptcha = computed(() => this.captchaResource.value()?.audio);
  /** Signal that is `true` while a CAPTCHA challenge is being fetched. */
  isLoading = this.captchaResource.isLoading;
  /** Signal that is `true` when fetching the CAPTCHA failed. */
  hasError = computed(
    () => !(this.captchaResource.isLoading() || this.captchaResource.hasValue())
  );
  /** Form control bound to the user's CAPTCHA answer; required and length-limited. */
  answer = new FormControl('', [
    Validators.required,
    Validators.maxLength(this.sizeValue),
  ]);
  /**
   * Supported languages, mapping the internal language code to the EU CAPTCHA
   * locale id and a human-readable name.
   */
  private readonly captchaLang = [
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

  /**
   * Syncs the plain {@link captchaId} / {@link captchaToken} fields from the
   * loaded {@link captchaResource}, and reloads the CAPTCHA whenever the
   * resolved {@link captchaLanguageId} changes (skipping the very first load,
   * which is handled automatically by {@link captchaResource} reacting to its
   * `params`).
   */
  constructor() {
    // Mirror the resource's id/token onto plain fields: the hosting component
    // reads them imperatively (without calling a signal) via `viewChild`.
    effect(() => {
      const challenge = this.captchaResource.value();
      if (challenge) {
        this.captchaId = challenge.id;
        this.captchaToken = challenge.token;
      }
    });

    // Reload captcha when language changes
    effect(() => {
      // Track the computed value to trigger on language changes
      this.captchaLanguageId();
      // Only reload if we already have a captcha loaded (skip initial load).
      // Read captchaImage untracked so this effect does not re-run when the
      // signal is updated as a side effect of reload(), which would loop
      // indefinitely.
      if (untracked(this.captchaImage)) {
        this.refresh();
      }
    });
  }

  /**
   * Reloads the CAPTCHA challenge, typically in response to a user-triggered
   * refresh action.
   */
  public refresh() {
    this.captchaResource.reload();
  }
}
