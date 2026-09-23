import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { HelpService, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ContactSupportComponent } from './contact-support.component';
import { ContactReasons } from './reasons-enum';

describe('ContactSupportComponent', () => {
  const mockHelpService = {
    contactSupport: vi.fn().mockReturnValue(of(undefined)),
    contactSupportAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(true),
    getCurrentUsername: vi.fn().mockReturnValue('guest'),
  };

  const mockUserService = {
    getUser: vi
      .fn()
      .mockReturnValue(
        of({ firstname: 'John', lastname: 'Doe', email: 'john@test.com' })
      ),
  };

  const mockRouter = {
    navigate: vi.fn().mockResolvedValue(true),
  };

  function createComponent() {
    TestBed.configureTestingModule({
      imports: [ContactSupportComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: HelpService, useValue: mockHelpService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UserService, useValue: mockUserService },
        { provide: Router, useValue: mockRouter },
      ],
    }).overrideComponent(ContactSupportComponent, {
      set: {
        imports: [ReactiveFormsModule, TranslocoModule],
        schemas: [NO_ERRORS_SCHEMA],
        template: '',
      },
    });

    const fixture = TestBed.createComponent(ContactSupportComponent);
    fixture.detectChanges();
    return fixture;
  }

  afterEach(() => {
    vi.clearAllMocks();
    mockLoginService.isGuest.mockReturnValue(true);
  });

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should initialize the contact form with required controls', () => {
    const { componentInstance: comp } = createComponent();
    expect(comp.contactForm).toBeDefined();
    expect(comp.contactForm.controls['reason']).toBeDefined();
    expect(comp.contactForm.controls['name']).toBeDefined();
    expect(comp.contactForm.controls['email']).toBeDefined();
    expect(comp.contactForm.controls['content']).toBeDefined();
  });

  it('should populate reasons from ContactReasons enum', () => {
    const { componentInstance: comp } = createComponent();
    expect(comp.reasons).toHaveLength(Object.keys(ContactReasons).length);
  });

  it('should return true for isGuest when login service says guest', () => {
    const { componentInstance: comp } = createComponent();
    expect(comp.isGuest()).toBe(true);
  });

  it('should fetch user data on init when not a guest', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    mockLoginService.getCurrentUsername.mockReturnValue('testuser');

    const { componentInstance: comp } = createComponent();

    expect(mockUserService.getUser).toHaveBeenCalledWith({
      userId: 'testuser',
    });
    expect(comp.contactForm.controls['name'].value).toBe('John Doe');
    expect(comp.contactForm.controls['email'].value).toBe('john@test.com');
  });

  it('should return true for needsSubject when reason is OTHER', () => {
    const { componentInstance: comp } = createComponent();
    comp.contactForm.controls['reason'].setValue(ContactReasons.OTHER);
    expect(comp.needsSubject()).toBe(true);
  });

  it('should return false for needsSubject when reason is not OTHER', () => {
    const { componentInstance: comp } = createComponent();
    comp.contactForm.controls['reason'].setValue(
      ContactReasons.UPLOAD_DOWNLOAD
    );
    expect(comp.needsSubject()).toBe(false);
  });

  it('should be disabled when form is invalid', () => {
    const { componentInstance: comp } = createComponent();
    expect(comp.isDisabled()).toBe(true);
  });

  it('should not be disabled when form is valid and user is not guest', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    const { componentInstance: comp } = createComponent();
    comp.contactForm.controls['reason'].setValue('test');
    comp.contactForm.controls['name'].setValue('Test');
    comp.contactForm.controls['email'].setValue('test@test.com');
    comp.contactForm.controls['content'].setValue('content');
    expect(comp.isDisabled()).toBe(false);
  });

  it('should call helpService.contactSupport and navigate on contact()', async () => {
    const { componentInstance: comp } = createComponent();
    comp.contactForm.controls['reason'].setValue('test');
    comp.contactForm.controls['name'].setValue('Test');
    comp.contactForm.controls['email'].setValue('test@test.com');
    comp.contactForm.controls['content'].setValue('content');

    await comp.contact();

    expect(mockHelpService.contactSupportAsync).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/help']);
  });

  it('should set isWrongCaptcha on invalid captcha error', async () => {
    mockHelpService.contactSupportAsync.mockImplementation(() => {
      throw { error: { message: 'invalid captcha answer' } };
    });

    const { componentInstance: comp } = createComponent();
    comp.contactForm.controls['reason'].setValue('test');
    comp.contactForm.controls['name'].setValue('Test');
    comp.contactForm.controls['email'].setValue('test@test.com');
    comp.contactForm.controls['content'].setValue('content');

    await comp.contact();

    expect(comp.isWrongCaptcha()).toBe(true);
    expect(comp.processing()).toBe(false);
  });

  it('should return file name from getFileName', () => {
    const { componentInstance: comp } = createComponent();
    comp.contactForm.controls['file'].setValue('test.pdf');
    expect(comp.getFileName()).toBe('test.pdf');
  });
});
