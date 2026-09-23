import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { SupportComponent } from './support.component';

describe('SupportComponent', () => {
  let component: SupportComponent;

  const mockRouter = { url: '/support/faq' };
  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({ properties: { isAdmin: 'false' } }),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SupportComponent],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: LoginService, useValue: mockLoginService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(SupportComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(SupportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should return true when router url includes the given part', () => {
    expect(component.isRoute('faq')).toBe(true);
  });

  it('should return false when router url does not include the given part', () => {
    expect(component.isRoute('contact')).toBe(false);
  });
});
