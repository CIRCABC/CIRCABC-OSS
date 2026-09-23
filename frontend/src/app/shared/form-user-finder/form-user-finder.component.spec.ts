import { Component, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { FormUserFinderComponent } from './form-user-finder.component';

const mockUser: User = {
  userId: 'testuser',
  firstname: 'John',
  lastname: 'Doe',
  email: 'john.doe@ec.europa.eu',
  properties: { domain: 'internal' },
};

const mockInterestGroup: InterestGroup = {
  name: 'Test Group',
  permissions: { library: 'LibAdmin' },
};

const mockLoginService = {
  getUser: vi.fn().mockReturnValue({ properties: { domain: 'internal' } }),
};

const mockUserService = {
  getUsersAsync: vi.fn().mockResolvedValue([mockUser]),
};

const mockGroupsService = {
  getInterestGroupAsync: vi.fn().mockResolvedValue(mockInterestGroup),
};

const routeParams$ = new Subject<Record<string, string>>();

const mockActivatedRoute = {
  params: routeParams$.asObservable(),
};

@Component({
  template: `<cbc-form-user-finder [searchAdvancedForm]="form" />`,
  imports: [FormUserFinderComponent],
})
class TestHostComponent {
  form = new FormGroup({ creatorUser: new FormControl<User | null>(null) });
  finder = viewChild.required(FormUserFinderComponent);
}

describe('FormUserFinderComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let component: FormUserFinderComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: UserService, useValue: mockUserService },
        { provide: InterestGroupService, useValue: mockGroupsService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();
    component = fixture.componentInstance.finder();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should build the addUserForm on init', () => {
    expect(component.addUserForm).toBeDefined();
    expect(component.addUserForm.controls['name']).toBeDefined();
    expect(component.addUserForm.controls['possibleUsers']).toBeDefined();
  });

  it('should set isExternalUser based on login service domain', () => {
    expect(component.isExternalUser).toBe(false);
  });

  it('should set isExternalUser to true for external domain', async () => {
    mockLoginService.getUser.mockReturnValue({
      properties: { domain: 'external' },
    });

    const fix2 = TestBed.createComponent(TestHostComponent);
    fix2.detectChanges();
    const comp2 = fix2.componentInstance.finder();

    expect(comp2.isExternalUser).toBe(true);

    // restore
    mockLoginService.getUser.mockReturnValue({
      properties: { domain: 'internal' },
    });
  });

  it('should load interest group when route has id param', async () => {
    routeParams$.next({ id: '123' });
    await fixture.whenStable();

    expect(mockGroupsService.getInterestGroupAsync).toHaveBeenCalledWith({
      id: '123',
    });
    expect(component.currentGroup).toEqual(mockInterestGroup);
  });

  describe('searchUsers', () => {
    it('should populate users when name is not empty and user is internal', async () => {
      component.addUserForm.controls['name'].setValue('john');
      await component.searchUsers();

      expect(mockUserService.getUsersAsync).toHaveBeenCalledWith({
        query: 'john',
      });
      expect(component.availableUsers()).toEqual([mockUser]);
      expect(component.searchingUsers()).toBe(false);
    });

    it('should not search when name is empty', async () => {
      mockUserService.getUsersAsync.mockClear();
      component.addUserForm.controls['name'].setValue('');
      await component.searchUsers();

      expect(mockUserService.getUsersAsync).not.toHaveBeenCalled();
    });

    it('should require valid email for external users', async () => {
      component.isExternalUser = true;
      mockUserService.getUsersAsync.mockClear();
      component.addUserForm.controls['name'].setValue('notanemail');
      await component.searchUsers();

      expect(mockUserService.getUsersAsync).not.toHaveBeenCalled();
    });

    it('should search with valid email for external users', async () => {
      component.isExternalUser = true;
      component.addUserForm.controls['name'].setValue('user@test.com');
      await component.searchUsers();

      expect(mockUserService.getUsersAsync).toHaveBeenCalledWith({
        query: 'user@test.com',
      });
    });
  });

  describe('populateUsers', () => {
    it('should set noAvailableUsers when no results', async () => {
      mockUserService.getUsersAsync.mockResolvedValue([]);
      await component.populateUsers('nobody');

      expect(component.noAvailableUsers()).toBe(true);
      expect(component.availableUsers()).toEqual([]);
      expect(component.searchingUsers()).toBe(false);
    });

    it('should set noAccess on error', async () => {
      mockUserService.getUsersAsync.mockRejectedValue(new Error('forbidden'));
      await component.populateUsers('test');

      expect(component.noAccess()).toBe(true);
      expect(component.searchingUsers()).toBe(false);
    });
  });

  describe('resetForm', () => {
    it('should clear all state', () => {
      component.availableUsers.set([mockUser]);
      component.noAvailableUsers.set(true);
      component.noAccess.set(true);
      component.addUserForm.controls['name'].setValue('test');

      component.resetForm();

      expect(component.availableUsers()).toEqual([]);
      expect(component.addUserForm.controls['name'].value).toBe('');
      expect(component.addUserForm.controls['possibleUsers'].value).toBe('');
      expect(component.noAvailableUsers()).toBe(false);
      expect(component.noAccess()).toBe(false);
    });
  });

  describe('selectUser', () => {
    it('should set userSelected and update parent form', async () => {
      await component.selectUser(mockUser);

      expect(component.userSelected()).toEqual(mockUser);
      expect(
        fixture.componentInstance.form.controls['creatorUser'].value
      ).toEqual(mockUser);
    });
  });

  describe('removeUser', () => {
    it('should clear userSelected and reset parent form', () => {
      component.userSelected.set(mockUser);
      component.removeUser();

      expect(component.userSelected()).toBeNull();
      expect(
        fixture.componentInstance.form.controls['creatorUser'].value
      ).toBeNull();
    });
  });

  describe('isSearchEmpty', () => {
    it('should return true when name is empty', () => {
      component.addUserForm.controls['name'].setValue('');
      expect(component.isSearchEmpty()).toBe(true);
    });

    it('should return false when name has value', () => {
      component.addUserForm.controls['name'].setValue('test');
      expect(component.isSearchEmpty()).toBe(false);
    });
  });

  describe('isValidEmail', () => {
    it('should return true for valid email', () => {
      component.addUserForm.controls['name'].setValue('user@example.com');
      expect(component.isValidEmail()).toBe(true);
    });

    it('should return false for invalid email', () => {
      component.addUserForm.controls['name'].setValue('notanemail');
      expect(component.isValidEmail()).toBe(false);
    });
  });

  it('should reset when parent form sets creatorUser to undefined', () => {
    component.addUserForm.controls['name'].setValue('test');
    component.availableUsers.set([mockUser]);

    fixture.componentInstance.form.controls['creatorUser'].setValue(
      undefined as unknown as User | null
    );

    expect(component.userSelected()).toBeUndefined();
    expect(component.availableUsers()).toEqual([]);
  });
});
