import { TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { I18nService } from './i18n.service';

describe('I18nService', () => {
  let service: I18nService;
  const mockTranslocoService = {
    getActiveLang: vi.fn().mockReturnValue('en'),
    getDefaultLang: vi.fn().mockReturnValue('en'),
    langChanges$: of('en'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        I18nService,
        { provide: TranslocoService, useValue: mockTranslocoService },
      ],
    });
    service = TestBed.inject(I18nService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should return active language', () => {
    expect(service.getActiveLang()).toBe('en');
    expect(mockTranslocoService.getActiveLang).toHaveBeenCalled();
  });

  it('should return default language', () => {
    expect(service.getDefaultLang()).toBe('en');
    expect(mockTranslocoService.getDefaultLang).toHaveBeenCalled();
  });

  it('should return lang changes observable', () => {
    let emitted: string | undefined;
    service.getLangChanges$().subscribe((lang: string) => {
      emitted = lang;
    });
    expect(emitted).toBe('en');
  });
});
