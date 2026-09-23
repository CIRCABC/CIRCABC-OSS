import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockLoginService: {
    isGuest: ReturnType<typeof vi.fn>;
    login: ReturnType<typeof vi.fn>;
  };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };
  let mockRedirectionService: { redirect: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockLoginService = {
      isGuest: vi.fn().mockReturnValue(true),
      login: vi.fn().mockResolvedValue(true),
    };
    mockRouter = { navigate: vi.fn().mockResolvedValue(true) };
    mockRedirectionService = { redirect: vi.fn().mockResolvedValue(undefined) };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter },
        { provide: RedirectionService, useValue: mockRedirectionService },
        { provide: ActivatedRoute, useValue: {} },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to /me on init if user is not guest', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    const newFixture = TestBed.createComponent(LoginComponent);
    newFixture.detectChanges();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/me']);
  });

  it('should not redirect on init if user is guest', () => {
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should build the login form with username and password controls', () => {
    expect(component.loginForm).toBeDefined();
    expect(component.loginForm.get('username')).toBeDefined();
    expect(component.loginForm.get('password')).toBeDefined();
  });

  it('should not call loginService.login if form is invalid', async () => {
    await component.login();
    expect(mockLoginService.login).not.toHaveBeenCalled();
    expect(component.submitClicked()).toBe(true);
  });

  it('should call loginService.login and redirect on success', async () => {
    component.loginForm.setValue({ username: 'user', password: 'pass' });
    await component.login();
    expect(mockLoginService.login).toHaveBeenCalledWith({
      username: 'user',
      password: 'pass',
    });
    expect(mockRedirectionService.redirect).toHaveBeenCalled();
    expect(component.loggingIn()).toBe(false);
  });

  it('should set loginDenied when login returns false', async () => {
    mockLoginService.login.mockResolvedValue(false);
    component.loginForm.setValue({ username: 'user', password: 'pass' });
    await component.login();
    expect(component.loginDenied()).toBe(true);
    expect(mockRedirectionService.redirect).not.toHaveBeenCalled();
  });

  it('should set loginError and reset form when login throws', async () => {
    mockLoginService.login.mockRejectedValue(new Error('network error'));
    component.loginForm.setValue({ username: 'user', password: 'pass' });
    await component.login();
    expect(component.loginError()).toBe(true);
    expect(component.loginForm.value).toEqual({
      username: '',
      password: '',
    });
    expect(component.loggingIn()).toBe(false);
  });
});
