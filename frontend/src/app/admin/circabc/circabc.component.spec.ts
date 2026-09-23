import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { CiracbcAdminReloadListenerService } from 'app/core/circabc-admin-reload-listener.service';
import { CircabcService, User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { CircabcComponent } from './circabc.component';

const mockAdmins: User[] = [
  { userId: 'admin1', firstname: 'John', lastname: 'Doe' },
  { userId: 'admin2', firstname: 'Jane', lastname: 'Smith' },
];

describe('CircabcComponent', () => {
  let refreshSubject: Subject<void>;

  const mockCircabcService = {
    getCircabAdministratorsAsync: vi.fn().mockResolvedValue(mockAdmins),
    deleteCircabcAdministratorsAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({
      userId: 'user1',
      properties: { isAdmin: 'true' },
    } as User),
  };

  beforeEach(() => {
    refreshSubject = new Subject<void>();

    TestBed.configureTestingModule({
      imports: [CircabcComponent],
      providers: [
        { provide: CircabcService, useValue: mockCircabcService },
        { provide: LoginService, useValue: mockLoginService },
        {
          provide: CiracbcAdminReloadListenerService,
          useValue: { refreshAnnounced$: refreshSubject.asObservable() },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(CircabcComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    vi.clearAllMocks();
    mockCircabcService.getCircabAdministratorsAsync.mockResolvedValue(
      mockAdmins
    );
    mockCircabcService.deleteCircabcAdministratorsAsync.mockResolvedValue(
      undefined
    );
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(CircabcComponent);
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should load administrators on init', async () => {
    const fixture: ComponentFixture<CircabcComponent> =
      TestBed.createComponent(CircabcComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockCircabcService.getCircabAdministratorsAsync).toHaveBeenCalled();
    expect(fixture.componentInstance.administrators()).toEqual(mockAdmins);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('should reload administrators when refresh is announced', async () => {
    const fixture: ComponentFixture<CircabcComponent> =
      TestBed.createComponent(CircabcComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    vi.clearAllMocks();
    mockCircabcService.getCircabAdministratorsAsync.mockResolvedValue(
      mockAdmins
    );

    refreshSubject.next();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockCircabcService.getCircabAdministratorsAsync).toHaveBeenCalled();
  });

  it('should delete an administrator and reload the list', async () => {
    const fixture: ComponentFixture<CircabcComponent> =
      TestBed.createComponent(CircabcComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    await fixture.componentInstance.deleteAdministrator({ userId: 'admin1' });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(
      mockCircabcService.deleteCircabcAdministratorsAsync
    ).toHaveBeenCalledWith({ userId: 'admin1' });
    expect(mockCircabcService.getCircabAdministratorsAsync).toHaveBeenCalled();
  });

  it('should return true from isAdmin when user is admin', () => {
    const fixture = TestBed.createComponent(CircabcComponent);
    expect(fixture.componentInstance.isAdmin()).toBe(true);
  });

  it('should return false from isAdmin when user is not admin', () => {
    mockLoginService.getUser.mockReturnValue({
      userId: 'user1',
      properties: { isAdmin: 'false' },
    } as User);
    const fixture = TestBed.createComponent(CircabcComponent);
    expect(fixture.componentInstance.isAdmin()).toBe(false);
  });

  it('should unsubscribe on destroy', async () => {
    const fixture: ComponentFixture<CircabcComponent> =
      TestBed.createComponent(CircabcComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    fixture.componentInstance.ngOnDestroy();
    // After destroy, emitting should not trigger another load
    vi.clearAllMocks();
    refreshSubject.next();
    expect(
      mockCircabcService.getCircabAdministratorsAsync
    ).not.toHaveBeenCalled();
  });
});
