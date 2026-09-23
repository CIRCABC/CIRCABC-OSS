import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import {
  MembersService,
  Profile,
  ProfileService,
} from 'app/core/generated/circabc';
import { SelectableUserProfile } from 'app/core/ui-model';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { I18nService } from 'app/shared/services/i18n.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ChangeProfilesMultipleComponent } from './change-profiles-multiple.component';

const mockProfiles: Profile[] = [
  { id: 'p1', name: 'admin', title: { en: 'Admin' } },
  { id: 'p2', name: 'author', title: { en: 'Author' } },
  { id: 'p3', name: 'guest', title: { en: 'Guest' } },
  { id: 'p4', name: 'EVERYONE', title: { en: 'Everyone' } },
];

const mockUsers: SelectableUserProfile[] = [
  { user: { userId: 'u1' }, profile: { id: 'p1', name: 'admin' } },
  { user: { userId: 'u2' }, profile: { id: 'p2', name: 'author' } },
];

describe('ChangeProfilesMultipleComponent', () => {
  let component: ChangeProfilesMultipleComponent;
  let componentRef: ComponentRef<ChangeProfilesMultipleComponent>;
  let fixture: ComponentFixture<ChangeProfilesMultipleComponent>;

  const mockProfileService = {
    getProfilesAsync: vi.fn().mockResolvedValue(mockProfiles),
  };

  const mockMembersService = {
    putMember: vi.fn().mockReturnValue(of(undefined)),
    putMemberAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockI18nService = {
    getActiveLang: vi.fn().mockReturnValue('en'),
    getDefaultLang: vi.fn().mockReturnValue('en'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangeProfilesMultipleComponent],
      providers: [
        { provide: ProfileService, useValue: mockProfileService },
        { provide: MembersService, useValue: mockMembersService },
        { provide: I18nService, useValue: mockI18nService },
        I18nPipe,
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ChangeProfilesMultipleComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('users', mockUsers);
    componentRef.setInput('groupId', 'group1');
    componentRef.setInput('showModal', true);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  it('should load profiles on init excluding guest and EVERYONE', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockProfileService.getProfilesAsync).toHaveBeenCalledWith({
      id: 'group1',
    });
    expect(component.profiles()).toEqual([
      { id: 'p1', name: 'admin', title: { en: 'Admin' } },
      { id: 'p2', name: 'author', title: { en: 'Author' } },
    ]);
  });

  it('should not load profiles if groupId is empty', async () => {
    componentRef.setInput('groupId', '');
    mockProfileService.getProfilesAsync.mockClear();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockProfileService.getProfilesAsync).not.toHaveBeenCalled();
    expect(component.profiles()).toEqual([]);
  });

  it('should change profiles successfully and emit SUCCEED', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.changeProfileForm.patchValue({ selectedProfile: 'p2' });

    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    await component.changeProfiles();

    expect(mockMembersService.putMemberAsync).toHaveBeenCalledWith({
      id: 'group1',
      membershipPostDefinition: {
        adminNotifications: false,
        userNotifications: false,
        memberships: expect.any(Array),
      },
    });
    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.CHANGE_PROFILE,
      result: ActionResult.SUCCEED,
    });
    expect(component.processing()).toBe(false);
  });

  it('should emit FAILED when putMember throws', async () => {
    mockMembersService.putMemberAsync.mockRejectedValueOnce(new Error('fail'));
    fixture.detectChanges();
    await fixture.whenStable();
    component.changeProfileForm.patchValue({ selectedProfile: 'p1' });

    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    await component.changeProfiles();

    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.CHANGE_PROFILE,
      result: ActionResult.FAILED,
    });
    expect(component.processing()).toBe(false);
  });

  it('should emit CANCELED on cancel', () => {
    fixture.detectChanges();
    const emitSpy = vi.spyOn(component.modalHide, 'emit');
    component.cancel();
    expect(emitSpy).toHaveBeenCalledWith({
      type: ActionType.CHANGE_PROFILE,
      result: ActionResult.CANCELED,
    });
  });

  it('should return the selected profile from getProfile', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.changeProfileForm.patchValue({ selectedProfile: 'p2' });
    const result = component.getProfile();
    expect(result).toEqual({
      id: 'p2',
      name: 'author',
      title: { en: 'Author' },
    });
  });

  it('should return undefined from getProfile when no match', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.changeProfileForm.patchValue({ selectedProfile: 'nonexistent' });
    expect(component.getProfile()).toBeUndefined();
  });

  it('should return profile name when title transform is empty', () => {
    fixture.detectChanges();
    const member = { profile: { name: 'author', title: {} } };
    expect(component.getProfileTitle(member)).toBe('author');
  });

  it('should return empty string when profile is undefined', () => {
    fixture.detectChanges();
    expect(component.getProfileTitle({})).toBe('');
  });

  it('should return empty string when profile name is undefined', () => {
    fixture.detectChanges();
    const member = { profile: { title: {} } };
    expect(component.getProfileTitle(member)).toBe('');
  });
});
