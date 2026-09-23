import { Location } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AccessDeniedComponent } from './access-denied.component';

describe('AccessDeniedComponent', () => {
  const mockLoginService = { isGuest: vi.fn().mockReturnValue(true) };
  const mockEULoginService = { euLogin: vi.fn() };
  const mockLocation = { back: vi.fn() };
  const mockRouter = { navigate: vi.fn().mockResolvedValue(true) };

  let component: AccessDeniedComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AccessDeniedComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: EULoginService, useValue: mockEULoginService },
        { provide: Location, useValue: mockLocation },
        { provide: Router, useValue: mockRouter },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(AccessDeniedComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(AccessDeniedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create the component', () => {
    expect(component).toBeDefined();
  });

  it('should set isGuest to true when user is guest', () => {
    expect(component.isGuest).toBe(true);
  });

  it('should set isGuest to false when user is not guest', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    component.ngOnInit();
    expect(component.isGuest).toBe(false);
  });

  it('should call location.back() on goBack', () => {
    component.goBack();
    expect(mockLocation.back).toHaveBeenCalled();
  });

  it('should navigate to /login on goToLogin', async () => {
    await component.goToLogin();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should call euLoginService.euLogin on euLogin', () => {
    component.euLogin();
    expect(mockEULoginService.euLogin).toHaveBeenCalled();
  });
});
