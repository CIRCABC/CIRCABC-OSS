import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
  TranslocoService,
} from '@jsverse/transloco';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { LegalNoticeComponent } from './legal-notice.component';

describe('LegalNoticeComponent', () => {
  let xhrStatus: number;
  const originalXHR = globalThis.XMLHttpRequest;

  beforeEach(() => {
    xhrStatus = 200;
    globalThis.XMLHttpRequest = class {
      readyState = 4;
      get status() {
        return xhrStatus;
      }
      open = vi.fn();
      send = vi.fn();
    } as unknown as typeof XMLHttpRequest;
  });

  afterEach(() => {
    globalThis.XMLHttpRequest = originalXHR;
  });

  function createComponent(paramValue: string | null = null) {
    const mockParamMap = {
      get: vi
        .fn()
        .mockImplementation((key: string) =>
          key === 'link' ? paramValue : null
        ),
    };

    TestBed.configureTestingModule({
      imports: [LegalNoticeComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { paramMap: of(mockParamMap) } },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(LegalNoticeComponent, {
      set: {
        imports: [TranslocoModule, SetTitlePipe],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    const fixture = TestBed.createComponent(LegalNoticeComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should default step to privacy', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance.step()).toBe('privacy');
  });

  it('should set step to terms when route param is terms', () => {
    const fixture = createComponent('terms');
    expect(fixture.componentInstance.step()).toBe('terms');
  });

  it('should set step to accessibility when route param is accessibility', () => {
    const fixture = createComponent('accessibility');
    expect(fixture.componentInstance.step()).toBe('accessibility');
  });

  it('should set url exists flags to true when XHR returns 200', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance.urlPSExists).toBe(true);
    expect(fixture.componentInstance.urlTOSExists).toBe(true);
    expect(fixture.componentInstance.urlACCESSExists).toBe(true);
  });

  it('should set url exists flags to false when XHR returns 404', () => {
    xhrStatus = 404;
    const fixture = createComponent();
    expect(fixture.componentInstance.urlPSExists).toBe(false);
    expect(fixture.componentInstance.urlTOSExists).toBe(false);
    expect(fixture.componentInstance.urlACCESSExists).toBe(false);
  });

  it('should build correct PDF URLs using active language', () => {
    const fixture = createComponent();
    const lang = TestBed.inject(TranslocoService).getActiveLang();
    expect(fixture.componentInstance.urlPS()).toContain(`ps/ps-${lang}.pdf`);
    expect(fixture.componentInstance.urlTOS()).toContain(`tos/tos-${lang}.pdf`);
    expect(fixture.componentInstance.urlACCESS()).toContain(
      `access/access-${lang}.pdf`
    );
  });
});
