import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  InterestGroupService,
  MembersService,
  Profile,
  ProfileService,
  User,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UsersPickerComponent } from './users-picker.component';

const mockUser: User = {
  userId: 'user1',
  firstname: 'John',
  lastname: 'Doe',
  email: 'john@example.com',
};

const mockProfile: Profile = {
  name: 'admin',
  title: { en: 'Administrator' },
};

const mockPagedUserProfile = {
  data: [{ user: mockUser, profile: mockProfile }],
  total: 1,
};

const mockInterestGroup = {
  name: 'TestGroup',
  permissions: { library: 'LibAdmin' },
};

const mockMembersService = {
  getMembersAsync: vi.fn().mockResolvedValue(mockPagedUserProfile),
};

const mockProfileService = {
  getProfilesAsync: vi.fn().mockResolvedValue([mockProfile]),
};

const mockGroupService = {
  getInterestGroupAsync: vi.fn().mockResolvedValue(mockInterestGroup),
};

const mockPermEvalService = {
  isDirAdmin: vi.fn().mockReturnValue(false),
  isDirManageMembers: vi.fn().mockReturnValue(false),
};

const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

const mockI18nPipe = {
  transform: vi.fn().mockReturnValue(''),
};

describe('UsersPickerComponent', () => {
  let component: UsersPickerComponent;
  let fixture: ComponentFixture<UsersPickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsersPickerComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: MembersService, useValue: mockMembersService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: I18nPipe, useValue: mockI18nPipe },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UsersPickerComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize with empty arrays', () => {
    expect(component.availableUsersOrProfiles()).toEqual([]);
    expect(component.selectedUsersOrProfiles()).toEqual([]);
    expect(component.form).toBeDefined();
  });

  it('should load group when igId is set', async () => {
    fixture.componentRef.setInput('igId', 'ig123');
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockGroupService.getInterestGroupAsync).toHaveBeenCalledWith({
      id: 'ig123',
    });
    expect(component.currentGroup()).toEqual(mockInterestGroup);
  });

  describe('getCode', () => {
    it('should return userId for a User', () => {
      expect(component.getCode(mockUser)).toBe('user1');
    });

    it('should return name for a Profile', () => {
      expect(component.getCode(mockProfile)).toBe('admin');
    });
  });

  describe('getName', () => {
    it('should return full name for a User', () => {
      expect(component.getName(mockUser)).toBe('John  Doe');
    });

    it('should return name for a Profile', () => {
      expect(component.getName(mockProfile)).toBe('admin');
    });
  });

  describe('getNameEmail', () => {
    it('should return string as-is for string input', () => {
      expect(component.getNameEmail('test@example.com')).toBe(
        'test@example.com'
      );
    });

    it('should return name without email when not dirAdmin', () => {
      mockPermEvalService.isDirAdmin.mockReturnValue(false);
      mockPermEvalService.isDirManageMembers.mockReturnValue(false);
      expect(component.getNameEmail(mockUser)).toBe('John Doe');
    });

    it('should return name with email when dirAdmin', () => {
      mockPermEvalService.isDirAdmin.mockReturnValue(true);
      expect(component.getNameEmail(mockUser)).toBe(
        'John Doe (john@example.com)'
      );
    });

    it('should return profile title from i18nPipe when available', () => {
      mockI18nPipe.transform.mockReturnValue('Administrator');
      expect(component.getNameEmail(mockProfile)).toBe('Administrator');
    });

    it('should return profile name when i18nPipe returns empty', () => {
      mockI18nPipe.transform.mockReturnValue('');
      expect(component.getNameEmail(mockProfile)).toBe('admin');
    });
  });

  describe('onTypeChange', () => {
    it('should update selectedTypeValue', () => {
      component.onTypeChange('1');
      expect(component.selectedTypeValue).toBe('1');
    });
  });

  describe('doSearch', () => {
    beforeEach(() => {
      fixture.componentRef.setInput('igId', 'ig123');
      fixture.detectChanges();
    });

    it('should search users when selectedTypeValue is 0', async () => {
      component.selectedTypeValue = '0';
      component.searchText = 'john';
      await component.doSearch();
      expect(mockMembersService.getMembersAsync).toHaveBeenCalledWith({
        id: 'ig123',
        searchQuery: 'john',
      });
      expect(component.availableUsersOrProfiles()).toHaveLength(1);
      expect(component.availableUsersOrProfiles()[0].item).toEqual(mockUser);
    });

    it('should search profiles when selectedTypeValue is 1', async () => {
      component.selectedTypeValue = '1';
      component.searchText = 'admin';
      await component.doSearch();
      expect(mockProfileService.getProfilesAsync).toHaveBeenCalledWith({
        id: 'ig123',
        searchQuery: 'admin',
        nonEmptyProfiles: false,
      });
      expect(component.availableUsersOrProfiles()).toHaveLength(1);
    });

    it('should filter guest and EVERYONE profiles when displayGuestRegistered is false', async () => {
      const profiles: Profile[] = [
        { name: 'admin' },
        { name: 'guest' },
        { name: 'EVERYONE' },
      ];
      mockProfileService.getProfilesAsync.mockResolvedValue(profiles);
      component.selectedTypeValue = '1';
      await component.doSearch();
      expect(component.availableUsersOrProfiles()).toHaveLength(1);
      expect(component.availableUsersOrProfiles()[0].item).toEqual({
        name: 'admin',
      });
    });
  });

  describe('addToSelectedUsersOrProfiles', () => {
    it('should add selected available items to selectedUsersOrProfiles', () => {
      component.availableUsersOrProfiles.set([
        { item: mockUser, selected: true },
        { item: mockProfile, selected: false },
      ]);
      component.addToSelectedUsersOrProfiles();
      expect(component.selectedUsersOrProfiles()).toEqual([mockUser]);
    });

    it('should not add duplicates', () => {
      component.selectedUsersOrProfiles.set([mockUser]);
      component.availableUsersOrProfiles.set([
        { item: mockUser, selected: true },
      ]);
      component.addToSelectedUsersOrProfiles();
      expect(component.selectedUsersOrProfiles()).toHaveLength(1);
    });
  });

  describe('removeFromSelectedUsersOrProfiles', () => {
    it('should remove item from selectedUsersOrProfiles', () => {
      component.selectedUsersOrProfiles.set([mockUser, mockProfile]);
      component.removeFromSelectedUsersOrProfiles(mockUser);
      expect(component.selectedUsersOrProfiles()).toEqual([mockProfile]);
    });
  });

  describe('clearAvailableUsersOrProfiles', () => {
    it('should clear the available list', () => {
      component.availableUsersOrProfiles.set([
        { item: mockUser, selected: true },
      ]);
      component.clearAvailableUsersOrProfiles();
      expect(component.availableUsersOrProfiles()).toEqual([]);
    });
  });

  describe('writeValue (ControlValueAccessor)', () => {
    it('should reset state when value is null', async () => {
      component.selectedUsersOrProfiles.set([mockUser]);
      await component.writeValue(null);
      expect(component.selectedUsersOrProfiles()).toEqual([]);
      expect(component.availableUsersOrProfiles()).toEqual([]);
    });
  });

  describe('registerOnChange / registerOnTouched', () => {
    it('should register onChange callback', () => {
      const fn = vi.fn();
      component.registerOnChange(fn);
      component.onChange('test');
      expect(fn).toHaveBeenCalledWith('test');
    });

    it('should register onTouched callback', () => {
      const fn = vi.fn();
      component.registerOnTouched(fn);
      component.onTouched();
      expect(fn).toHaveBeenCalled();
    });
  });

  describe('setSelected', () => {
    it('should mark items as selected based on multiselect element', () => {
      component.availableUsersOrProfiles.set([
        { item: mockUser, selected: false },
        { item: mockProfile, selected: false },
      ]);
      const mockElement = {
        options: [{ selected: true }, { selected: false }],
      };
      component.setSelected(mockElement);
      expect(component.availableUsersOrProfiles()[0].selected).toBe(true);
      expect(component.availableUsersOrProfiles()[1].selected).toBe(false);
    });
  });
});
