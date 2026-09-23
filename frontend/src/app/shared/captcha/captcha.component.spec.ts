import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { CaptchaControllerService } from 'app/core/generated/eu-captcha';
import { CaptchaResultDto } from 'app/core/generated/eu-captcha/model/captchaResultDto';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { CaptchaComponent } from './captcha.component';

const mockCaptchaResult: CaptchaResultDto = {
  captchaId: 'test-id-123',
  captchaImg: 'base64ImageData',
  captchaType: 'STANDARD',
  audioCaptcha: 'base64AudioData',
};

const mockResponse = new HttpResponse({
  body: mockCaptchaResult,
  headers: new HttpHeaders({ 'x-jwtString': 'test-token' }),
});

describe('CaptchaComponent', () => {
  let mockCaptchaService: { getCaptchaImageUsingGET: ReturnType<typeof vi.fn> };

  function createComponent() {
    mockCaptchaService = {
      getCaptchaImageUsingGET: vi.fn().mockReturnValue(of(mockResponse)),
    };

    TestBed.configureTestingModule({
      imports: [CaptchaComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: CaptchaControllerService, useValue: mockCaptchaService },
      ],
    }).overrideComponent(CaptchaComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(CaptchaComponent);
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should load captcha on init', async () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;

    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockCaptchaService.getCaptchaImageUsingGET).toHaveBeenCalled();
    expect(component.captchaImage()).toBe(
      'data:image/png;base64,base64ImageData'
    );
    expect(component.captchaId).toBe('test-id-123');
    expect(component.captchaToken).toBe('test-token');
    expect(component.isLoading()).toBe(false);
    expect(component.hasError()).toBe(false);
  });

  it('should set hasError on failure', async () => {
    const fixture = createComponent();
    mockCaptchaService.getCaptchaImageUsingGET.mockReturnValue(
      throwError(() => new Error('fail'))
    );

    fixture.detectChanges();
    await fixture.whenStable();

    expect(fixture.componentInstance.hasError()).toBe(true);
    expect(fixture.componentInstance.isLoading()).toBe(false);
  });

  it('should reload captcha on refresh', async () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    mockCaptchaService.getCaptchaImageUsingGET.mockClear();

    component.refresh();
    await fixture.whenStable();

    expect(mockCaptchaService.getCaptchaImageUsingGET).toHaveBeenCalled();
  });

  it('should validate answer as required with maxLength 8', () => {
    const fixture = createComponent();
    const answer = fixture.componentInstance.answer;

    answer.setValue('');
    expect(answer.valid).toBe(false);

    answer.setValue('123456789');
    expect(answer.hasError('maxlength')).toBe(true);

    answer.setValue('12345678');
    expect(answer.valid).toBe(true);
  });

  it('should compute captchaLanguageId from languageCode signal', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;

    component.languageCode.set('fr');
    expect(component.captchaLanguageId()).toBe('fr-FR');

    component.languageCode.set('de');
    expect(component.captchaLanguageId()).toBe('de-DE');
  });

  it('should default captchaLanguageId to en-GB for unknown codes', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;

    component.languageCode.set('xx');
    expect(component.captchaLanguageId()).toBe('en-GB');
  });
});
