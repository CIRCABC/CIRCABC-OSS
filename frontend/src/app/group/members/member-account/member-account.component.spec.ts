import { Location } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { User, UserService } from 'app/core/generated/circabc';
import { BehaviorSubject, of } from 'rxjs';
import { vi } from 'vitest';
import { MemberAccountComponent } from './member-account.component';

const mockUser: User = {
  userId: 'testuser',
  firstname: 'John',
  lastname: 'Doe',
  email: 'john@example.com',
};

describe('MemberAccountComponent', () => {
  const params$ = new BehaviorSubject({ userid: 'testuser' });
  const mockUserService = { getUserAsync: vi.fn().mockResolvedValue(mockUser) };
  const mockLocation = { back: vi.fn() };

  function createComponent() {
    TestBed.configureTestingModule({
      imports: [MemberAccountComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { params: params$ } },
        { provide: UserService, useValue: mockUserService },
        { provide: Location, useValue: mockLocation },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(MemberAccountComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(MemberAccountComponent);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(() => {
    mockUserService.getUserAsync.mockClear();
    mockLocation.back.mockClear();
    params$.next({ userid: 'testuser' });
  });

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should load member on init', async () => {
    const fixture = createComponent();
    await fixture.whenStable();

    expect(mockUserService.getUserAsync).toHaveBeenCalledWith({
      userId: 'testuser',
    });
    expect(fixture.componentInstance.user()).toEqual(mockUser);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('should not load member if userid is empty', async () => {
    params$.next({ userid: '' });
    mockUserService.getUserAsync.mockClear();
    const fixture = createComponent();
    await fixture.whenStable();

    expect(mockUserService.getUserAsync).not.toHaveBeenCalled();
  });

  it('should go back when goBack is called', () => {
    const fixture = createComponent();
    fixture.componentInstance.goBack();
    expect(mockLocation.back).toHaveBeenCalled();
  });
});
