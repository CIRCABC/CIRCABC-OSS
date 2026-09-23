import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { LogoutComponent } from './logout.component';

describe('LogoutComponent', () => {
  const mockLoginService = { logout: vi.fn().mockResolvedValue(true) };
  const mockEULoginService = { logout: vi.fn() };
  const mockRouter = { navigate: vi.fn() };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LogoutComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: EULoginService, useValue: mockEULoginService },
        { provide: Router, useValue: mockRouter },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });
    vi.clearAllMocks();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(LogoutComponent);
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should call loginService.logout on init', async () => {
    const fixture = TestBed.createComponent(LogoutComponent);
    await fixture.componentInstance.performLogout();
    expect(mockLoginService.logout).toHaveBeenCalled();
  });

  it('should navigate to /welcome when circabcRelease is oss', async () => {
    const original = environment.circabcRelease;
    (environment as { circabcRelease: string }).circabcRelease = 'oss';

    const fixture = TestBed.createComponent(LogoutComponent);
    await fixture.componentInstance.performLogout();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/welcome']);
    (environment as { circabcRelease: string }).circabcRelease = original;
  });

  it('should call euloginservice.logout when circabcRelease is not oss', async () => {
    const original = environment.circabcRelease;
    (environment as { circabcRelease: string }).circabcRelease = 'ent';

    const fixture = TestBed.createComponent(LogoutComponent);
    await fixture.componentInstance.performLogout();

    expect(mockEULoginService.logout).toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
    (environment as { circabcRelease: string }).circabcRelease = original;
  });
});
