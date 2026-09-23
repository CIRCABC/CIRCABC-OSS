import { TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { CiracbcAdminReloadListenerService } from 'app/core/circabc-admin-reload-listener.service';
import { CircabcService, User, UserService } from 'app/core/generated/circabc';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { CreateCircabcComponent } from './create-circabc.component';

describe('CreateCircabcComponent', () => {
  let component: CreateCircabcComponent;

  const mockUserService = {
    getUsersAsync: vi.fn().mockResolvedValue([]),
  };

  const mockCircabcService = {
    postCircabcAdministrators: vi.fn().mockReturnValue(of(undefined)),
    postCircabcAdministratorsAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockReloadService = {
    propagateCircabcAdminRefresh: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateCircabcComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: CircabcService, useValue: mockCircabcService },
        {
          provide: CiracbcAdminReloadListenerService,
          useValue: mockReloadService,
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(CreateCircabcComponent);
    component = fixture.componentInstance;
    await component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize the form on ngOnInit', () => {
    expect(component.addUserForm).toBeDefined();
    expect(component.addUserForm.controls['name'].value).toBe('');
    expect(component.addUserForm.controls['possibleUsers'].value).toBe('');
  });

  it('should reset state on cancel', () => {
    component.futureAdmins = [{ userId: 'user1' }];
    component.availableUsers.set([{ userId: 'user2' }]);

    component.cancel();

    expect(component.futureAdmins).toEqual([]);
    expect(component.availableUsers()).toEqual([]);
  });

  it('should search users when name is not empty', async () => {
    const users: User[] = [{ userId: 'u1', firstname: 'John' }];
    mockUserService.getUsersAsync.mockResolvedValue(users);

    component.addUserForm.controls['name'].setValue('John');
    await component.searchUsers();

    expect(mockUserService.getUsersAsync).toHaveBeenCalledWith({
      query: 'John',
      filter: false,
    });
    expect(component.availableUsers()).toEqual(users);
  });

  it('should not search users when name is empty', async () => {
    mockUserService.getUsersAsync.mockClear();
    component.addUserForm.controls['name'].setValue('');

    await component.searchUsers();

    expect(mockUserService.getUsersAsync).not.toHaveBeenCalled();
  });

  it('should reset form fields', () => {
    component.availableUsers.set([{ userId: 'u1' }]);
    component.addUserForm.controls['name'].setValue('test');
    component.addUserForm.controls['possibleUsers'].setValue('val');

    component.resetForm();

    expect(component.availableUsers()).toEqual([]);
    expect(component.addUserForm.controls['name'].value).toBe('');
    expect(component.addUserForm.controls['possibleUsers'].value).toBe('');
  });

  it('should select users from available users and add to futureAdmins', () => {
    component.availableUsers.set([{ userId: 'u1' }, { userId: 'u2' }]);
    component.addUserForm.controls['possibleUsers'].setValue(['u1']);

    component.selectUsers();

    expect(component.futureAdmins).toEqual([{ userId: 'u1' }]);
  });

  it('should not add duplicate users to futureAdmins', () => {
    component.futureAdmins = [{ userId: 'u1' }];
    component.availableUsers.set([{ userId: 'u1' }]);
    component.addUserForm.controls['possibleUsers'].setValue(['u1']);

    component.selectUsers();

    expect(component.futureAdmins).toHaveLength(1);
  });

  it('should remove user from futureAdmins', () => {
    const user: User = { userId: 'u1' };
    component.futureAdmins = [user];

    component.removeFromFutureAdmin(user);

    expect(component.futureAdmins).toEqual([]);
  });

  it('should return true from isWizardOk when futureAdmins is not empty', () => {
    component.futureAdmins = [{ userId: 'u1' }];
    expect(component.isWizardOk()).toBe(true);
  });

  it('should return false from isWizardOk when futureAdmins is empty', () => {
    component.futureAdmins = [];
    expect(component.isWizardOk()).toBe(false);
  });

  it('should return true from hasSelectedUser when possibleUsers has value', () => {
    component.addUserForm.controls['possibleUsers'].setValue('u1');
    expect(component.hasSelectedUser()).toBe(true);
  });

  it('should return false from hasSelectedUser when possibleUsers is empty', () => {
    component.addUserForm.controls['possibleUsers'].setValue('');
    expect(component.hasSelectedUser()).toBe(false);
  });

  it('should invite admins and refresh on success', async () => {
    component.futureAdmins = [{ userId: 'u1' }, { userId: 'u2' }];
    mockCircabcService.postCircabcAdministratorsAsync.mockResolvedValue(
      undefined
    );

    await component.inviteCircabcAdmin();

    expect(
      mockCircabcService.postCircabcAdministratorsAsync
    ).toHaveBeenCalledWith({ requestBody: ['u1', 'u2'] });
    expect(mockReloadService.propagateCircabcAdminRefresh).toHaveBeenCalled();
    expect(component.processing()).toBe(false);
  });

  it('should handle error during inviteCircabcAdmin', async () => {
    component.futureAdmins = [{ userId: 'u1' }];
    mockCircabcService.postCircabcAdministrators.mockReturnValue(
      throwError(() => new Error('fail'))
    );

    await component.inviteCircabcAdmin();

    expect(component.processing()).toBe(false);
  });
});
