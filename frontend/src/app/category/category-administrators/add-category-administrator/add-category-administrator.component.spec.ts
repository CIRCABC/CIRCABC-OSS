import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { CategoryService, User, UserService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddCategoryAdministratorComponent } from './add-category-administrator.component';

const mockUsers: User[] = [
  { userId: 'user1', firstname: 'John', lastname: 'Doe' },
  { userId: 'user2', firstname: 'Jane', lastname: 'Smith' },
];

const mockCategoryService = {
  getCategoryAdministratorsAsync: vi.fn().mockResolvedValue([]),
  postCategoryAdministartors: vi.fn().mockReturnValue(of(null)),
  postCategoryAdministartorsAsync: vi.fn().mockResolvedValue(null),
};

const mockUserService = {
  getUsersAsync: vi.fn().mockResolvedValue(mockUsers),
};

describe('AddCategoryAdministratorComponent', () => {
  let component: AddCategoryAdministratorComponent;
  let fixture: ComponentFixture<AddCategoryAdministratorComponent>;
  let componentRef: ComponentRef<AddCategoryAdministratorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCategoryAdministratorComponent, ReactiveFormsModule],
      providers: [
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: UserService, useValue: mockUserService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    vi.clearAllMocks();
    mockCategoryService.getCategoryAdministratorsAsync.mockResolvedValue([]);
    mockCategoryService.postCategoryAdministartors.mockReturnValue(of(null));
    mockCategoryService.postCategoryAdministartorsAsync.mockResolvedValue(null);
    mockUserService.getUsersAsync.mockResolvedValue(mockUsers);

    fixture = TestBed.createComponent(AddCategoryAdministratorComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('showModal', true);
    componentRef.setInput('categoryId', 'cat1');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form and load existing admins on init', async () => {
    const admins: User[] = [{ userId: 'admin1' }];
    mockCategoryService.getCategoryAdministratorsAsync.mockResolvedValue(
      admins
    );
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.addUserForm).toBeDefined();
    expect(component.existingAdmins()).toEqual(admins);
    expect(
      mockCategoryService.getCategoryAdministratorsAsync
    ).toHaveBeenCalledWith({
      id: 'cat1',
    });
  });

  it('should search users and populate availableUsers', async () => {
    component.addUserForm.controls['name'].setValue('john');

    await component.searchUsers();

    expect(mockUserService.getUsersAsync).toHaveBeenCalledWith({
      query: 'john',
      filter: true,
    });
    expect(component.availableUsers()).toEqual(mockUsers);
    expect(component.searchingUsers()).toBe(false);
  });

  it('should not search users when name is empty', async () => {
    component.addUserForm.controls['name'].setValue('');

    await component.searchUsers();

    expect(mockUserService.getUsersAsync).not.toHaveBeenCalled();
  });

  it('should add admins successfully', async () => {
    component.futureMembers.set([{ userId: 'user1' }, { userId: 'user2' }]);

    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    await component.addAdmins();

    expect(
      mockCategoryService.postCategoryAdministartorsAsync
    ).toHaveBeenCalledWith({ id: 'cat1', requestBody: ['user1', 'user2'] });
    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.INVITE_CATEGORY_ADMIN,
      result: ActionResult.SUCCEED,
    });
    expect(component.futureMembers()).toEqual([]);
    expect(component.processing()).toBe(false);
  });

  it('should handle addAdmins failure', async () => {
    mockCategoryService.postCategoryAdministartorsAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    component.futureMembers.set([{ userId: 'user1' }]);

    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    await component.addAdmins();

    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.INVITE_CATEGORY_ADMIN,
      result: ActionResult.FAILED,
    });
    expect(component.processing()).toBe(false);
  });

  it('should emit canceled result on cancel', async () => {
    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    component.cancel();

    expect(emitSpy).toHaveBeenCalledWith({
      result: ActionResult.CANCELED,
      type: ActionType.INVITE_CATEGORY_ADMIN,
    });
    expect(component.futureMembers()).toEqual([]);
  });

  it('should detect existing admin', async () => {
    mockCategoryService.getCategoryAdministratorsAsync.mockResolvedValue([
      { userId: 'admin1' },
    ]);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.isAlreadyAdmin({ userId: 'admin1' })).toBe(true);
    expect(component.isAlreadyAdmin({ userId: 'other' })).toBe(false);
  });

  it('should select users from possibleUsers into futureMembers', async () => {
    component.availableUsers.set(mockUsers);
    component.addUserForm.controls['possibleUsers'].setValue([
      'user1',
      'user2',
    ]);

    component.selectUsers();

    expect(component.futureMembers()).toEqual(mockUsers);
  });

  it('should not add duplicate users to futureMembers', async () => {
    component.availableUsers.set(mockUsers);
    component.futureMembers.set([
      { userId: 'user1', firstname: 'John', lastname: 'Doe' },
    ]);
    component.addUserForm.controls['possibleUsers'].setValue([
      'user1',
      'user2',
    ]);

    component.selectUsers();

    expect(component.futureMembers()).toHaveLength(2);
  });

  it('should remove user from futureMembers', async () => {
    const user: User = { userId: 'user1' };
    component.futureMembers.set([user, { userId: 'user2' }]);

    component.removeFromFutureMember(user);

    expect(component.futureMembers()).toEqual([{ userId: 'user2' }]);
  });

  it('should reset form correctly', async () => {
    component.availableUsers.set(mockUsers);
    component.addUserForm.controls['name'].setValue('test');

    component.resetForm();

    expect(component.availableUsers()).toEqual([]);
    expect(component.addUserForm.controls['name'].value).toBe('');
    expect(component.addUserForm.controls['possibleUsers'].value).toBe('');
  });

  it('hasSelectedUser should return false when possibleUsers is empty', async () => {
    component.addUserForm.controls['possibleUsers'].setValue('');

    expect(component.hasSelectedUser()).toBe(false);
  });

  it('hasSelectedUser should return true when possibleUsers has value', async () => {
    component.addUserForm.controls['possibleUsers'].setValue(['user1']);

    expect(component.hasSelectedUser()).toBe(true);
  });
});
