import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  InterestGroupProfile,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UserManagementComponent } from './user-management.component';

const mockUser: User = { userId: 'user1', firstname: 'John', lastname: 'Doe' };
const mockUser2: User = {
  userId: 'user2',
  firstname: 'Jane',
  lastname: 'Smith',
};

const mockMembership: InterestGroupProfile = {
  profile: { name: 'MEMBER' },
  interestGroup: { name: 'TestGroup', permissions: {} },
};

const mockUserService = {
  getUsersAsync: vi.fn().mockResolvedValue([mockUser, mockUser2]),
  getUserAsync: vi.fn().mockResolvedValue(mockUser),
  getUsersFromListAsync: vi.fn().mockResolvedValue([mockUser]),
  getUserMembership: vi.fn().mockReturnValue(of([mockMembership])),
};

describe('UserManagementComponent', () => {
  let component: UserManagementComponent;

  beforeEach(() => {
    vi.clearAllMocks();

    TestBed.configureTestingModule({
      imports: [UserManagementComponent, ReactiveFormsModule],
      providers: [
        { provide: UserService, useValue: mockUserService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    const fixture = TestBed.createComponent(UserManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and initialize forms', () => {
    expect(component).toBeDefined();
    expect(component.searchForm).toBeDefined();
    expect(component.fileForm).toBeDefined();
    expect(component.searchStep).toBe('search');
  });

  describe('searchUsers', () => {
    it('should search users and set results', async () => {
      component.searchForm.controls['searchField'].setValue('john');
      await component.searchUsers();
      expect(mockUserService.getUsersAsync).toHaveBeenCalledWith({
        query: 'john',
        filter: false,
      });
      expect(component.searchedUsers()).toEqual([mockUser, mockUser2]);
      expect(component.searching()).toBe(false);
    });

    it('should not call service when search field is empty', async () => {
      component.searchForm.controls['searchField'].setValue('');
      await component.searchUsers();
      expect(mockUserService.getUsersAsync).not.toHaveBeenCalled();
      expect(component.searching()).toBe(false);
    });

    it('should handle errors gracefully', async () => {
      mockUserService.getUsersAsync.mockRejectedValueOnce(new Error('fail'));
      component.searchForm.controls['searchField'].setValue('john');
      await component.searchUsers();
      expect(component.searching()).toBe(false);
    });
  });

  describe('focusUser', () => {
    it('should set focused user when userId is provided', async () => {
      await component.focusUser('user1');
      expect(mockUserService.getUserAsync).toHaveBeenCalledWith({
        userId: 'user1',
      });
      expect(component.focusedUser()).toEqual(mockUser);
      expect(component.oneUserFocus()).toBe(true);
    });

    it('should clear focused user when userId is empty', async () => {
      await component.focusUser('');
      expect(component.focusedUserId()).toBe('');
      expect(component.oneUserFocus()).toBe(false);
      expect(component.focusedUser()).toBeUndefined();
    });

    it('should set expectionNoAlfrescoUser on specific error', async () => {
      mockUserService.getUserAsync.mockRejectedValueOnce({
        error: {
          message: 'User does not exist and could not be created',
        },
      });
      await component.focusUser('user1');
      expect(component.expectionNoAlfrescoUser()).toBe(true);
    });
  });

  describe('selection', () => {
    it('should add user to selectedUsers with memberships', () => {
      component.selection(mockUser);
      expect(component.selectedUsers()).toHaveLength(1);
      expect(component.selectedUsers()[0].userid).toBe('user1');
      expect(mockUserService.getUserMembership).toHaveBeenCalledWith({
        userId: 'user1',
        language: '',
        lightMode: false,
      });
    });

    it('should not add duplicate user', () => {
      component.selection(mockUser);
      component.selection(mockUser);
      expect(component.selectedUsers()).toHaveLength(1);
    });
  });

  describe('remove', () => {
    it('should remove user from selectedUsers', () => {
      component.selectedUsers.set([
        {
          userid: 'user1',
          user: mockUser,
          memberships: [],
          loadingMemberships: false,
        },
      ]);
      component.remove(mockUser);
      expect(component.selectedUsers()).toHaveLength(0);
    });
  });

  describe('resetSearch', () => {
    it('should clear search field and results', () => {
      component.searchForm.controls['searchField'].setValue('test');
      component.searchedUsers.set([mockUser]);
      component.resetSearch();
      expect(component.searchForm.controls['searchField'].value).toBe('');
      expect(component.searchedUsers()).toEqual([]);
    });
  });

  describe('toggleProfile', () => {
    it('should set selected on matching membership', () => {
      component.selectedUsers.set([
        {
          userid: 'user1',
          user: mockUser,
          memberships: [{ ...mockMembership, selected: false }],
          loadingMemberships: false,
        },
      ]);
      component.toggleProfile('user1', 'MEMBER', 'TestGroup');
      expect(component.selectedUsers()[0].memberships[0].selected).toBe(true);
    });

    it('should return early if profileName or interestGroupName is undefined', () => {
      component.selectedUsers.set([
        {
          userid: 'user1',
          user: mockUser,
          memberships: [{ ...mockMembership, selected: false }],
          loadingMemberships: false,
        },
      ]);
      component.toggleProfile('user1', undefined, 'TestGroup');
      expect(component.selectedUsers()[0].memberships[0].selected).toBe(false);
    });
  });

  describe('toggleProfiles', () => {
    it('should select all memberships when type is not reset', () => {
      component.selectedUsers.set([
        {
          userid: 'user1',
          user: mockUser,
          memberships: [{ ...mockMembership, selected: false }],
          loadingMemberships: false,
        },
      ]);
      component.toggleProfiles('user1', 'all');
      expect(component.selectedUsers()[0].memberships[0].selected).toBe(true);
    });

    it('should deselect all memberships when type is reset', () => {
      component.selectedUsers.set([
        {
          userid: 'user1',
          user: mockUser,
          memberships: [{ ...mockMembership, selected: true }],
          loadingMemberships: false,
        },
      ]);
      component.toggleProfiles('user1', 'reset');
      expect(component.selectedUsers()[0].memberships[0].selected).toBe(false);
    });
  });

  describe('prepareRevocation', () => {
    it('should populate selectedUserIds and show modal', () => {
      component.selectedUsers.set([
        {
          userid: 'user1',
          user: mockUser,
          memberships: [],
          loadingMemberships: false,
        },
      ]);
      component.prepareRevocation();
      expect(component.selectedUserIds()).toEqual(['user1']);
      expect(component.showRevocationModal()).toBe(true);
    });
  });

  describe('refreshAfterSchedule', () => {
    it('should clear selections and hide modal', () => {
      component.selectedUserIds.set(['user1']);
      component.selectedUsers.set([
        {
          userid: 'user1',
          user: mockUser,
          memberships: [],
          loadingMemberships: false,
        },
      ]);
      component.showRevocationModal.set(true);
      component.refreshAfterSchedule();
      expect(component.selectedUserIds()).toEqual([]);
      expect(component.selectedUsers()).toEqual([]);
      expect(component.showRevocationModal()).toBe(false);
    });
  });

  describe('selectAllSearch', () => {
    it('should select all searched users', () => {
      component.searchedUsers.set([mockUser, mockUser2]);
      component.selectAllSearch();
      expect(component.selectedUsers()).toHaveLength(2);
    });
  });
});
