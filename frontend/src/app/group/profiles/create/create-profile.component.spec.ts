import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { ProfileService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateProfileComponent } from './create-profile.component';

const mockProfileService = {
  postProfile: vi.fn().mockReturnValue(of({})),
  putProfile: vi.fn().mockReturnValue(of({})),
  postProfileAsync: vi.fn().mockResolvedValue({}),
  putProfileAsync: vi.fn().mockResolvedValue({}),
};

const basePermissions = {
  information: 'InfNoAccess',
  library: 'LibNoAccess',
  members: 'DirNoAccess',
  events: 'EveNoAccess',
  newsgroups: 'NwsNoAccess',
};

describe('CreateProfileComponent', () => {
  let component: CreateProfileComponent;
  let componentRef: ComponentRef<CreateProfileComponent>;
  let fixture: ComponentFixture<CreateProfileComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [CreateProfileComponent],
      providers: [
        { provide: ProfileService, useValue: mockProfileService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateProfileComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('groupId', 'group-123');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.createProfileForm.controls['title'].value).toBe('');
    expect(component.createProfileForm.controls['information'].value).toBe(0);
    expect(component.createProfileForm.controls['library'].value).toBe(0);
    expect(component.createProfileForm.controls['members'].value).toBe(0);
    expect(component.createProfileForm.controls['events'].value).toBe(0);
    expect(component.createProfileForm.controls['newsgroups'].value).toBe(0);
  });

  describe('cancelWizard', () => {
    it('should reset form and emit canceled', () => {
      const canceledSpy = vi.spyOn(component.canceled, 'emit');
      component.cancelWizard();
      expect(component.showDialog()).toBe(false);
      expect(component.profileToEdit()).toBeUndefined();
      expect(canceledSpy).toHaveBeenCalled();
    });
  });

  describe('isEditable', () => {
    it('should return true when no profileToEdit', () => {
      expect(component.isEditable()).toBe(true);
    });

    it('should return false for leader profile', () => {
      componentRef.setInput('profileToEdit', {
        name: 'IGLeader',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.isEditable()).toBe(false);
    });

    it('should return false for full admin profile', () => {
      componentRef.setInput('profileToEdit', {
        name: 'SomeAdmin',
        permissions: {
          ...basePermissions,
          library: 'LibAdmin',
          information: 'InfAdmin',
          events: 'EveAdmin',
          forums: 'NwsAdmin',
          members: 'DirAdmin',
        },
      });
      fixture.detectChanges();
      expect(component.isEditable()).toBe(false);
    });

    it('should return true for a regular profile', () => {
      componentRef.setInput('profileToEdit', {
        name: 'Contributor',
        permissions: { ...basePermissions, library: 'LibAccess' },
      });
      fixture.detectChanges();
      expect(component.isEditable()).toBe(true);
    });
  });

  describe('isEdition', () => {
    it('should return false when no profileToEdit', () => {
      expect(component.isEdition()).toBe(false);
    });

    it('should return true when profileToEdit has id', () => {
      componentRef.setInput('profileToEdit', {
        id: 'prof-1',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.isEdition()).toBe(true);
    });
  });

  describe('getLabel', () => {
    it('should return label.create when no profileToEdit', () => {
      expect(component.getLabel()).toBe('label.create');
    });

    it('should return label.save when editing', () => {
      componentRef.setInput('profileToEdit', {
        id: 'prof-1',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.getLabel()).toBe('label.save');
    });
  });

  describe('permission index/key conversions', () => {
    it('should convert information permission index to key', () => {
      expect(component.getInfPerms(0)).toBe('InfNoAccess');
      expect(component.getInfPerms(3)).toBe('InfAdmin');
    });

    it('should convert information permission key to index', () => {
      expect(component.getInfIndex('InfAccess')).toBe(1);
    });

    it('should convert library permission index to key', () => {
      expect(component.getLibPerms(0)).toBe('LibNoAccess');
      expect(component.getLibPerms(5)).toBe('LibAdmin');
    });

    it('should convert library permission key to index', () => {
      expect(component.getLibIndex('LibFullEdit')).toBe(4);
    });

    it('should convert member permission index to key', () => {
      expect(component.getMemPerms(0)).toBe('DirNoAccess');
    });

    it('should convert event permission index to key', () => {
      expect(component.getEvtPerms(1)).toBe('EveAccess');
    });

    it('should convert newsgroup permission index to key', () => {
      expect(component.getNwsPerms(2)).toBe('NwsPost');
    });
  });

  describe('createOrEditProfile', () => {
    it('should create a new profile and emit profileCreated', async () => {
      const createdSpy = vi.spyOn(component.profileCreated, 'emit');
      component.createProfileForm.controls['title'].setValue({ en: 'Test' });
      component.createProfileForm.controls['library'].setValue(1);

      await component.createOrEditProfile();

      expect(mockProfileService.postProfileAsync).toHaveBeenCalledWith({
        id: 'group-123',
        profile: expect.objectContaining({
          title: { en: 'Test' },
          permissions: expect.objectContaining({
            library: 'LibAccess',
          }),
        }),
      });
      expect(createdSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.CREATE_PROFILE,
          result: ActionResult.SUCCEED,
        })
      );
      expect(component.processing()).toBe(false);
    });

    it('should update an existing profile and emit profileUpdated', async () => {
      const updatedSpy = vi.spyOn(component.profileUpdated, 'emit');
      componentRef.setInput('profileToEdit', {
        id: 'prof-1',
        permissions: { ...basePermissions, visibility: 'public' },
      });
      fixture.detectChanges();

      component.createProfileForm.controls['title'].setValue({ en: 'Edited' });

      await component.createOrEditProfile();

      expect(mockProfileService.putProfileAsync).toHaveBeenCalledWith({
        id: 'prof-1',
        profile: expect.objectContaining({
          id: 'prof-1',
          title: { en: 'Edited' },
        }),
      });
      expect(updatedSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.EDIT_PROFILE,
          result: ActionResult.SUCCEED,
        })
      );
    });

    it('should emit FAILED result on error', async () => {
      mockProfileService.postProfileAsync.mockRejectedValueOnce(
        new Error('fail')
      );

      const createdSpy = vi.spyOn(component.profileCreated, 'emit');
      await component.createOrEditProfile();

      expect(createdSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.CREATE_PROFILE,
          result: ActionResult.FAILED,
        })
      );
      expect(component.processing()).toBe(false);
    });
  });

  describe('max permissions', () => {
    it('should return full max for regular profiles', () => {
      expect(component.getMaxInfoPerm()).toBe(3);
      expect(component.getMaxLibPerm()).toBe(5);
      expect(component.getMaxMembersPerm()).toBe(3);
      expect(component.getMaxEventsPerm()).toBe(2);
      expect(component.getMaxForumsPerm()).toBe(4);
    });

    it('should return 1 for guest profiles', () => {
      componentRef.setInput('profileToEdit', {
        groupName: 'guest',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.getMaxInfoPerm()).toBe(1);
      expect(component.getMaxLibPerm()).toBe(1);
      expect(component.getMaxMembersPerm()).toBe(1);
      expect(component.getMaxEventsPerm()).toBe(1);
      expect(component.getMaxForumsPerm()).toBe(1);
    });
  });

  describe('isGuestOrRegistered', () => {
    it('should return false when no profileToEdit', () => {
      expect(component.isGuestOrRegistered()).toBe(false);
    });

    it('should return true for guest', () => {
      componentRef.setInput('profileToEdit', {
        name: 'guest',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.isGuestOrRegistered()).toBe(true);
    });

    it('should return true for EVERYONE', () => {
      componentRef.setInput('profileToEdit', {
        name: 'EVERYONE',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.isGuestOrRegistered()).toBe(true);
    });
  });

  describe('isGuest', () => {
    it('should return true for guest profile', () => {
      componentRef.setInput('profileToEdit', {
        name: 'guest',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.isGuest()).toBe(true);
    });

    it('should return false for non-guest', () => {
      componentRef.setInput('profileToEdit', {
        name: 'EVERYONE',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.isGuest()).toBe(false);
    });
  });

  describe('isRegistered', () => {
    it('should return true for EVERYONE profile', () => {
      componentRef.setInput('profileToEdit', {
        name: 'EVERYONE',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.isRegistered()).toBe(true);
    });

    it('should return false for non-EVERYONE', () => {
      componentRef.setInput('profileToEdit', {
        name: 'guest',
        permissions: { ...basePermissions },
      });
      fixture.detectChanges();
      expect(component.isRegistered()).toBe(false);
    });
  });
});
