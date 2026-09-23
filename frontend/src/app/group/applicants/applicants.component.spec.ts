import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Applicant,
  InterestGroup,
  InterestGroupProfile,
  InterestGroupService,
  MembersService,
  Profile,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ApplicantsComponent } from './applicants.component';

const mockGroup: InterestGroup = {
  id: 'group1',
  name: 'Test Group',
  permissions: { directory: 'DirAdmin' },
};

const mockApplicants: Applicant[] = [
  { user: { userId: 'user1' }, justification: 'reason1' },
  { user: { userId: 'user2' }, justification: 'reason2' },
  { user: { userId: 'user3' }, justification: 'reason3' },
];

const mockProfiles: Profile[] = [
  { name: 'admin' },
  { name: 'guest' },
  { name: 'EVERYONE' },
  { name: 'member' },
];

describe('ApplicantsComponent', () => {
  let component: ApplicantsComponent;
  let fixture: ComponentFixture<ApplicantsComponent>;
  let paramsSubject: Subject<{ [key: string]: string }>;

  const mockMembersService = {
    getApplicantAsync: vi.fn().mockResolvedValue(mockApplicants),
  };

  const mockGroupsService = {
    getInterestGroupAsync: vi.fn().mockResolvedValue(mockGroup),
  };

  const mockProfileService = {
    getProfilesAsync: vi.fn().mockResolvedValue(mockProfiles),
  };

  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(false),
    getUser: vi.fn().mockReturnValue({ userId: 'currentUser' }),
  };

  const mockUserService = {
    getUserMembershipAsync: vi.fn().mockResolvedValue([
      {
        interestGroup: {
          id: 'group1',
          name: 'Test Group',
          permissions: {},
        },
        profile: { name: 'admin' },
      },
    ] as InterestGroupProfile[]),
  };

  const mockPermEvalService = {
    isDirAdmin: vi.fn().mockReturnValue(true),
    isDirManageMembers: vi.fn().mockReturnValue(false),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [ApplicantsComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: MembersService, useValue: mockMembersService },
        { provide: InterestGroupService, useValue: mockGroupsService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UserService, useValue: mockUserService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicantsComponent);
    component = fixture.componentInstance;
  });

  async function initComponent(id = 'group1') {
    fixture.detectChanges();
    paramsSubject.next({ id });
    await fixture.whenStable();
  }

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('applicants and profiles loading', () => {
    it('should load applicants and set currentApplicant to first', async () => {
      await initComponent();
      expect(mockGroupsService.getInterestGroupAsync).toHaveBeenCalledWith({
        id: 'group1',
      });
      expect(mockMembersService.getApplicantAsync).toHaveBeenCalledWith({
        id: 'group1',
      });
      expect(component.applicants()).toEqual(mockApplicants);
      expect(component.currentApplicant()).toEqual(mockApplicants[0]);
      expect(component.currentIndex()).toBe(1);
    });

    it('should load profiles and filter out guest and EVERYONE', async () => {
      await initComponent();
      expect(mockProfileService.getProfilesAsync).toHaveBeenCalledWith({
        id: 'group1',
      });
      expect(component.profiles()).toEqual([
        { name: 'admin' },
        { name: 'member' },
      ]);
    });

    it('should set alreadyMember to true when user is member of the group', async () => {
      await initComponent();
      expect(component.alreadyMember()).toBe(true);
    });
  });

  describe('when user is guest', () => {
    it('should not check memberships', async () => {
      mockLoginService.isGuest.mockReturnValue(true);
      mockUserService.getUserMembershipAsync.mockClear();

      await initComponent();

      expect(mockUserService.getUserMembershipAsync).not.toHaveBeenCalled();
      expect(component.alreadyMember()).toBe(false);

      mockLoginService.isGuest.mockReturnValue(false);
    });
  });

  describe('navigation', () => {
    beforeEach(async () => {
      await initComponent();
    });

    it('nextPage should advance to next applicant', () => {
      component.nextPage();
      expect(component.currentIndex()).toBe(2);
      expect(component.currentApplicant()).toEqual(mockApplicants[1]);
      expect(component.state()).toBe('inc');
    });

    it('nextPage should wrap around to first when at end', () => {
      component.currentIndex.set(3);
      component.nextPage();
      expect(component.currentIndex()).toBe(1);
      expect(component.currentApplicant()).toEqual(mockApplicants[0]);
      expect(component.state()).toBe('dec');
    });

    it('previousPage should go to previous applicant', () => {
      component.currentIndex.set(2);
      component.currentApplicant.set(mockApplicants[1]);
      component.previousPage();
      expect(component.currentIndex()).toBe(1);
      expect(component.currentApplicant()).toEqual(mockApplicants[0]);
      expect(component.state()).toBe('dec');
    });

    it('previousPage should wrap around to last when at beginning', () => {
      component.previousPage();
      expect(component.currentIndex()).toBe(3);
      expect(component.currentApplicant()).toEqual(mockApplicants[2]);
      expect(component.state()).toBe('inc');
    });
  });

  describe('onRequestProcessed', () => {
    beforeEach(async () => {
      await initComponent();
    });

    it('should remove the current applicant from the list', () => {
      const removed = component.applicants()[0];
      component.onRequestProcessed(removed);
      expect(component.applicants()).toHaveLength(2);
      expect(component.applicants()).not.toContain(removed);
    });
  });

  describe('permission checks', () => {
    beforeEach(async () => {
      await initComponent();
    });

    it('isDirAdmin should delegate to permEvalService', () => {
      expect(component.isDirAdmin()).toBe(true);
      expect(mockPermEvalService.isDirAdmin).toHaveBeenCalledWith(mockGroup);
    });

    it('isDirManageMembers should delegate to permEvalService', () => {
      expect(component.isDirManageMembers()).toBe(false);
      expect(mockPermEvalService.isDirManageMembers).toHaveBeenCalledWith(
        mockGroup
      );
    });
  });

  describe('isMember', () => {
    it('should return alreadyMember value', async () => {
      mockUserService.getUserMembershipAsync.mockResolvedValueOnce([]);
      await initComponent();
      expect(component.isMember()).toBe(false);

      mockUserService.getUserMembershipAsync.mockResolvedValueOnce([
        {
          interestGroup: { id: 'group1', name: 'Test Group', permissions: {} },
          profile: { name: 'admin' },
        } as InterestGroupProfile,
      ]);
      await initComponent('group2');
      expect(component.isMember()).toBe(true);
    });
  });
});
