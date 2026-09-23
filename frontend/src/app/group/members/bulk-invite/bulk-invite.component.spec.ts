import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  InterestGroupService,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';

import { BulkInviteComponent } from './bulk-invite.component';

describe('BulkInviteComponent', () => {
  const mockCategory = { id: 'cat1', name: 'Category 1' };
  const mockProfile = { id: 'p1', name: 'Access', title: { en: 'Access' } };
  const mockGroup = { id: 'ig1', name: 'Group 1', permissions: {} };
  const mockUser = { userId: 'user1', firstname: 'John', lastname: 'Doe' };

  const paramsSubject = new Subject<{ [key: string]: string }>();

  const mockUserService = {
    getBulkInviteCategoriesAsync: vi.fn().mockResolvedValue([mockCategory]),
    getBulkInviteIGsAsync: vi.fn().mockResolvedValue([]),
    getBulkInviteMembersAsync: vi.fn().mockResolvedValue([]),
    getUserMembershipAsync: vi.fn().mockResolvedValue([]),
    bulkInviteUsersAsync: vi.fn().mockResolvedValue(undefined),
    bulkInviteUsersDigestFileAsync: vi.fn().mockResolvedValue([]),
  };

  const mockProfileService = {
    getProfilesAsync: vi.fn().mockResolvedValue([mockProfile]),
  };

  const mockInterestGroupService = {
    getInterestGroupAsync: vi.fn().mockResolvedValue(mockGroup),
  };

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue(mockUser),
    isGuest: vi.fn().mockReturnValue(false),
  };

  const mockPermEvalService = {
    isDirAdmin: vi.fn().mockReturnValue(false),
    isDirManageMembers: vi.fn().mockReturnValue(false),
  };

  const mockSaveAsService = {
    saveUrlAs: vi.fn(),
  };

  const mockUiMessageService = {
    addInfoMessage: vi.fn(),
  };

  const mockRouter = {
    navigate: vi.fn(),
  };

  const mockI18nPipe = {
    transform: vi.fn().mockReturnValue('translated'),
  };

  function createComponent(): ComponentFixture<BulkInviteComponent> {
    TestBed.configureTestingModule({
      imports: [BulkInviteComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: UserService, useValue: mockUserService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: Router, useValue: mockRouter },
        { provide: I18nPipe, useValue: mockI18nPipe },
        { provide: BASE_PATH, useValue: 'http://localhost' },
      ],
    }).overrideComponent(BulkInviteComponent, {
      set: {
        imports: [ReactiveFormsModule, TranslocoModule],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    const fixture = TestBed.createComponent(BulkInviteComponent);
    fixture.detectChanges();
    return fixture;
  }

  /**
   * Creates the component, emits the given route param and waits for the
   * resource-driven data load to settle.
   */
  async function createComponentWithIgId(
    igId = 'ig1'
  ): Promise<ComponentFixture<BulkInviteComponent>> {
    const fixture = createComponent();
    paramsSubject.next({ igId });
    fixture.detectChanges();
    await fixture.whenStable();
    return fixture;
  }

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should build the form with default values', () => {
    const fixture = createComponent();
    const comp = fixture.componentInstance;
    expect(comp.bulkInviteForm).toBeDefined();
    expect(comp.bulkInviteForm.controls['createNewProfiles'].value).toBe(false);
    expect(comp.bulkInviteForm.controls['notifyUsers'].value).toBe(false);
  });

  it('should load data when route params emit', async () => {
    const fixture = await createComponentWithIgId('ig1');

    const comp = fixture.componentInstance;
    expect(comp.igId()).toBe('ig1');
    expect(mockInterestGroupService.getInterestGroupAsync).toHaveBeenCalledWith(
      {
        id: 'ig1',
      }
    );
    expect(mockUserService.getBulkInviteCategoriesAsync).toHaveBeenCalledWith({
      username: 'user1',
    });
    expect(mockProfileService.getProfilesAsync).toHaveBeenCalledWith({
      id: 'ig1',
    });
  });

  it('should filter out guest, EVERYONE, and colon profiles from availableProfiles', async () => {
    mockProfileService.getProfilesAsync.mockResolvedValue([
      { name: 'Access' },
      { name: 'guest' },
      { name: 'EVERYONE' },
      { name: 'imported:profile' },
      { name: 'Author' },
    ]);

    const fixture = await createComponentWithIgId('ig1');

    const comp = fixture.componentInstance;
    expect(comp.availableProfiles().map((p) => p.name)).toEqual([
      'Access',
      'Author',
    ]);
  });

  it('should detect alreadyMember when user belongs to the group', async () => {
    mockUserService.getUserMembershipAsync.mockResolvedValue([
      { interestGroup: { id: 'ig1', name: 'G', permissions: {} } },
    ]);

    const fixture = await createComponentWithIgId('ig1');

    expect(fixture.componentInstance.alreadyMember()).toBe(true);
  });

  describe('canInvite', () => {
    it('should return true when at least one member has status ok', async () => {
      mockUserService.getBulkInviteMembersAsync.mockResolvedValue([
        { username: 'u1', status: 'ok' },
        { username: 'u2', status: 'error' },
      ]);
      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;
      comp.selectedInterestGroups.set([{ name: 'IG', value: 'ig2' }]);
      fixture.detectChanges();
      await fixture.whenStable();

      expect(comp.canInvite()).toBe(true);
    });

    it('should return false when no member has status ok', async () => {
      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;

      expect(comp.canInvite()).toBe(false);
    });
  });

  describe('selectAll', () => {
    async function withMembers(
      members: { username: string; selected?: boolean }[]
    ) {
      mockUserService.getBulkInviteMembersAsync.mockResolvedValue(members);
      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;
      comp.selectedInterestGroups.set([{ name: 'IG', value: 'ig2' }]);
      fixture.detectChanges();
      await fixture.whenStable();
      return { fixture, comp };
    }

    it('should select all members with a username', async () => {
      const { comp } = await withMembers([
        { username: 'u1' },
        { username: 'u2' },
      ]);

      comp.selectAll();

      expect(comp.allSelected).toBe(true);
      expect(comp.selectedMembers).toHaveLength(2);
    });

    it('should deselect all when already selected', async () => {
      const { comp } = await withMembers([{ username: 'u1', selected: true }]);
      comp.allSelected = true;
      comp.selectedMembers = [...comp.allMembers()];

      comp.selectAll();

      expect(comp.allSelected).toBe(false);
      expect(comp.selectedMembers).toHaveLength(0);
    });

    it('should do nothing when allMembers is empty', async () => {
      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;

      comp.selectAll();

      expect(comp.allSelected).toBe(false);
    });
  });

  describe('deleteMember', () => {
    it('should remove a member from both members and allMembers', async () => {
      const member = { username: 'u1' };
      mockUserService.getBulkInviteMembersAsync.mockResolvedValue([member]);
      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;
      comp.selectedInterestGroups.set([{ name: 'IG', value: 'ig2' }]);
      fixture.detectChanges();
      await fixture.whenStable();
      const target = comp.allMembers()[0];

      comp.deleteMember(target);
      fixture.detectChanges();

      expect(comp.members()).toHaveLength(0);
      expect(comp.allMembers()).toHaveLength(0);
      expect(comp.totalItems()).toBe(comp.listingOptions().limit);
    });
  });

  describe('deleteSelectedMembers', () => {
    it('should remove all selected members', async () => {
      mockUserService.getBulkInviteMembersAsync.mockResolvedValue([
        { username: 'u1', selected: true },
        { username: 'u2', selected: true },
      ]);
      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;
      comp.selectedInterestGroups.set([{ name: 'IG', value: 'ig2' }]);
      fixture.detectChanges();
      await fixture.whenStable();
      comp.selectedMembers = [...comp.allMembers()];

      comp.deleteSelectedMembers();
      fixture.detectChanges();

      expect(comp.members()).toHaveLength(0);
      expect(comp.allMembers()).toHaveLength(0);
      expect(comp.selectedMembers).toHaveLength(0);
      expect(comp.allSelected).toBe(false);
    });
  });

  describe('pagination', () => {
    async function withThreeMembers() {
      mockUserService.getBulkInviteMembersAsync.mockResolvedValue([
        { username: 'u1' },
        { username: 'u2' },
        { username: 'u3' },
      ]);
      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;
      comp.selectedInterestGroups.set([{ name: 'IG', value: 'ig2' }]);
      fixture.detectChanges();
      await fixture.whenStable();
      return { fixture, comp };
    }

    it('should paginate allMembers correctly', async () => {
      const { fixture, comp } = await withThreeMembers();
      comp.changeLimit(2);
      fixture.detectChanges();

      expect(comp.members()).toHaveLength(2);
      expect(comp.members()[0].username).toBe('u1');
      expect(comp.members()[1].username).toBe('u2');
    });

    it('should show second page', async () => {
      const { fixture, comp } = await withThreeMembers();
      comp.changeLimit(2);
      comp.goToPage(2);
      fixture.detectChanges();

      expect(comp.members()).toHaveLength(1);
      expect(comp.members()[0].username).toBe('u3');
    });

    it('should show all when limit is -1', async () => {
      const { fixture, comp } = await withThreeMembers();
      comp.changeLimit(-1);
      fixture.detectChanges();

      expect(comp.members()).toHaveLength(3);
    });
  });

  describe('setProfile', () => {
    it('should set profileId on the matching member', async () => {
      mockUserService.getBulkInviteMembersAsync.mockResolvedValue([
        { username: 'u1', profileId: 'Access' },
      ]);
      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;
      comp.selectedInterestGroups.set([{ name: 'IG', value: 'ig2' }]);
      fixture.detectChanges();
      await fixture.whenStable();

      comp.setProfile('Author', 'u1');

      expect(comp.members()[0].profileId).toBe('Author');
    });
  });

  describe('setSelectedProfiles', () => {
    it('should set profileId on all selected members', () => {
      const fixture = createComponent();
      const comp = fixture.componentInstance;
      comp.selectedMembers = [
        { username: 'u1', profileId: 'Access' },
        { username: 'u2', profileId: 'Access' },
      ];

      comp.setSelectedProfiles('Author');

      expect(comp.selectedMembers[0].profileId).toBe('Author');
      expect(comp.selectedMembers[1].profileId).toBe('Author');
    });
  });

  describe('close', () => {
    it('should reset state and navigate', async () => {
      mockUserService.getBulkInviteMembersAsync.mockResolvedValue([
        { username: 'u1' },
      ]);
      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;
      comp.selectedInterestGroups.set([{ name: 'IG', value: 'ig2' }]);
      fixture.detectChanges();
      await fixture.whenStable();
      comp.selectedMembers = [...comp.allMembers()];

      comp.close();
      fixture.detectChanges();

      expect(comp.allMembers()).toHaveLength(0);
      expect(comp.selectedMembers).toHaveLength(0);
      expect(mockRouter.navigate).toHaveBeenCalled();
    });
  });

  describe('getImportUsersFileTemplate', () => {
    it('should call saveAsService with correct url', () => {
      const fixture = createComponent();
      const comp = fixture.componentInstance;

      const result = comp.getImportUsersFileTemplate();

      expect(mockSaveAsService.saveUrlAs).toHaveBeenCalledWith(
        'http://localhost/users/bulkinvite/template',
        'template.xls'
      );
      expect(result).toBe(false);
    });
  });

  describe('isDirAdmin / isDirManageMembers', () => {
    it('should delegate to permEvalService', async () => {
      mockPermEvalService.isDirAdmin.mockReturnValue(true);
      mockPermEvalService.isDirManageMembers.mockReturnValue(true);

      const fixture = await createComponentWithIgId('ig1');
      const comp = fixture.componentInstance;

      expect(comp.isDirAdmin()).toBe(true);
      expect(comp.isDirManageMembers()).toBe(true);
    });
  });

  describe('trackMember', () => {
    it('should return username-profileId string', () => {
      const fixture = createComponent();
      const comp = fixture.componentInstance;
      expect(comp.trackMember(0, { username: 'u1', profileId: 'Access' })).toBe(
        'u1-Access'
      );
    });
  });

  describe('truncateProfileTitle', () => {
    it('should return empty string for undefined', () => {
      const fixture = createComponent();
      expect(fixture.componentInstance.truncateProfileTitle(undefined)).toBe(
        ''
      );
    });

    it('should call i18nPipe transform for defined title', () => {
      const fixture = createComponent();
      const title = { en: 'Some Long Profile Title' };
      fixture.componentInstance.truncateProfileTitle(title);
      expect(mockI18nPipe.transform).toHaveBeenCalledWith(title);
    });
  });
});
