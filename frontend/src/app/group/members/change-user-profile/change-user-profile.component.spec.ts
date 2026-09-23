import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import {
  MembersService,
  Profile,
  ProfileService,
  type UserProfile,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ChangeUserProfileComponent } from './change-user-profile.component';

const mockProfiles: Profile[] = [
  { id: 'p1', name: 'admin', title: { en: 'Admin' } },
  { id: 'p2', name: 'author', title: { en: 'Author' } },
  { id: 'p3', name: 'guest', title: {} },
  { id: 'p4', name: 'EVERYONE', title: {} },
];

const mockMember: UserProfile = {
  user: { userId: 'user1' },
  profile: { id: 'p1', name: 'admin', title: { en: 'Admin' } },
};

describe('ChangeUserProfileComponent', () => {
  let component: ChangeUserProfileComponent;
  let componentRef: ComponentRef<ChangeUserProfileComponent>;
  let fixture: ComponentFixture<ChangeUserProfileComponent>;

  const mockProfileService = {
    getProfilesAsync: vi.fn().mockResolvedValue(mockProfiles),
  };

  const mockMembersService = {
    putMember: vi.fn().mockReturnValue(of(undefined)),
    putMemberAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockI18nPipe = {
    transform: vi.fn(
      (mltext: { [key: string]: string } | undefined) => mltext?.['en'] ?? ''
    ),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangeUserProfileComponent],
      providers: [
        { provide: ProfileService, useValue: mockProfileService },
        { provide: MembersService, useValue: mockMembersService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ChangeUserProfileComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('groupId', 'group1');
    componentRef.setInput('member', { ...mockMember });
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('profiles loading', () => {
    it('should load profiles excluding guest and EVERYONE', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockProfileService.getProfilesAsync).toHaveBeenCalledWith({
        id: 'group1',
      });
      expect(component.profiles()).toHaveLength(2);
      expect(component.profiles().map((p) => p.name)).toEqual([
        'admin',
        'author',
      ]);
    });

    it('should initialize the form', async () => {
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.changeProfileForm).toBeDefined();
      expect(
        component.changeProfileForm.controls['selectedProfile'].value
      ).toBe('p1');
    });
  });

  describe('cancel', () => {
    it('should emit CANCELED result and hide modal', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancel();

      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.CHANGE_PROFILE,
        result: ActionResult.CANCELED,
      });
    });
  });

  describe('changeProfile', () => {
    beforeEach(async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      component.changeProfileForm.controls['selectedProfile'].setValue('p2');
    });

    it('should call putMember and emit SUCCEED on success', async () => {
      mockMembersService.putMemberAsync.mockResolvedValue(undefined);
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.changeProfile();

      expect(mockMembersService.putMemberAsync).toHaveBeenCalledWith({
        id: 'group1',
        membershipPostDefinition: expect.objectContaining({
          memberships: expect.any(Array),
        }),
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.CHANGE_PROFILE,
        result: ActionResult.SUCCEED,
      });
      expect(component.processing()).toBe(false);
      expect(component.showModal()).toBe(false);
    });

    it('should emit FAILED on error', async () => {
      mockMembersService.putMemberAsync.mockRejectedValue(new Error('fail'));
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.changeProfile();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.CHANGE_PROFILE,
        result: ActionResult.FAILED,
      });
      expect(component.processing()).toBe(false);
    });
  });

  describe('getProfile', () => {
    it('should return the matching profile', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      component.changeProfileForm.controls['selectedProfile'].setValue('p2');

      const result = component.getProfile();
      expect(result).toEqual(mockProfiles[1]);
    });

    it('should return undefined if no match', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      component.changeProfileForm.controls['selectedProfile'].setValue(
        'nonexistent'
      );

      expect(component.getProfile()).toBeUndefined();
    });
  });

  describe('getProfileTitle', () => {
    it('should return translated title', () => {
      fixture.detectChanges();
      const result = component.getProfileTitle(mockMember);
      expect(result).toBe('Admin');
    });

    it('should return profile name if title is empty', () => {
      mockI18nPipe.transform.mockReturnValue('');
      fixture.detectChanges();
      const member: UserProfile = { profile: { name: 'author', title: {} } };

      const result = component.getProfileTitle(member);
      expect(result).toBe('author');
      mockI18nPipe.transform.mockImplementation(
        (mltext: { [key: string]: string } | undefined) => mltext?.['en'] ?? ''
      );
    });

    it('should return empty string if profile is undefined', () => {
      fixture.detectChanges();
      const result = component.getProfileTitle({});
      expect(result).toBe('');
    });
  });
});
