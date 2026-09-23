import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  ContentService,
  GroupDeletionReport,
  InterestGroupService,
  ProfileService,
  SpaceService,
} from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { DeleteGroupComponent } from './delete-group.component';

const mockGroupService = {
  isGroupDeletableAsync: vi.fn(),
  deleteInterestGroupAsync: vi.fn().mockResolvedValue(undefined),
};

const mockContentService = {
  deleteCheckoutAsync: vi.fn().mockResolvedValue(undefined),
};

const mockSpaceService = {
  getShareSpacesAsync: vi.fn(),
  deleteShareSpaceAsync: vi.fn().mockResolvedValue(undefined),
};

const mockProfileService = {
  putProfileAsync: vi.fn().mockResolvedValue(undefined),
};

const mockRouter = {
  navigate: vi.fn(),
};

const paramsSubject = new Subject<Record<string, string>>();

describe('DeleteGroupComponent', () => {
  let component: DeleteGroupComponent;
  let fixture: ComponentFixture<DeleteGroupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteGroupComponent],
      providers: [
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: ContentService, useValue: mockContentService },
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
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

    fixture = TestBed.createComponent(DeleteGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should set igNode from route params', () => {
    paramsSubject.next({ id: 'node123' });
    expect(component.igNode).toBe('node123');
  });

  describe('verifyConditions', () => {
    it('should set conditions and verified on success', async () => {
      const report: GroupDeletionReport = {
        lockedNodes: [],
        sharedNodes: [],
        sharedProfiles: [],
      };
      mockGroupService.isGroupDeletableAsync.mockResolvedValue(report);
      component.igNode = 'node1';

      await component.verifyConditions();

      expect(component.conditions()).toEqual(report);
      expect(component.verified()).toBe(true);
      expect(component.verifying()).toBe(false);
    });

    it('should handle error gracefully', async () => {
      mockGroupService.isGroupDeletableAsync.mockRejectedValue('fail');
      component.igNode = 'node1';

      await component.verifyConditions();

      expect(component.verified()).toBe(false);
      expect(component.verifying()).toBe(false);
    });
  });

  describe('isReadyForDeletion', () => {
    it('should return true when all arrays are empty', () => {
      component.conditions.set({
        lockedNodes: [],
        sharedNodes: [],
        sharedProfiles: [],
      });
      expect(component.isReadyForDeletion()).toBe(true);
    });

    it('should return false when lockedNodes is not empty', () => {
      component.conditions.set({
        lockedNodes: [{ id: '1' }],
        sharedNodes: [],
        sharedProfiles: [],
      });
      expect(component.isReadyForDeletion()).toBe(false);
    });

    it('should return false when conditions is undefined', () => {
      component.conditions.set(undefined as unknown as GroupDeletionReport);
      expect(component.isReadyForDeletion()).toBe(false);
    });
  });

  describe('cleanLocks', () => {
    it('should call deleteCheckout for each locked node and clear the list', async () => {
      component.conditions.set({
        lockedNodes: [{ id: 'n1' }, { id: 'n2' }],
        sharedNodes: [],
        sharedProfiles: [],
      });
      mockContentService.deleteCheckoutAsync.mockResolvedValue(undefined);

      await component.cleanLocks();

      expect(mockContentService.deleteCheckoutAsync).toHaveBeenCalledTimes(2);
      expect(component.conditions().lockedNodes).toEqual([]);
      expect(component.cleaningLocks()).toBe(false);
    });
  });

  describe('cleanSharedNodes', () => {
    it('should remove shared spaces and re-verify', async () => {
      component.conditions.set({
        lockedNodes: [],
        sharedNodes: [{ id: 's1' }],
        sharedProfiles: [],
      });
      component.igNode = 'node1';
      mockSpaceService.getShareSpacesAsync.mockResolvedValue({
        data: [{ igId: 'ig1' }],
      });
      mockSpaceService.deleteShareSpaceAsync.mockResolvedValue(undefined);
      const freshReport: GroupDeletionReport = {
        lockedNodes: [],
        sharedNodes: [],
        sharedProfiles: [],
      };
      mockGroupService.isGroupDeletableAsync.mockResolvedValue(freshReport);

      await component.cleanSharedNodes();

      expect(mockSpaceService.deleteShareSpaceAsync).toHaveBeenCalledWith({
        id: 's1',
        sharedIGId: 'ig1',
      });
      expect(component.cleaningSharedNodes()).toBe(false);
    });
  });

  describe('deleteGroup', () => {
    it('should call deleteInterestGroup and navigate on success', async () => {
      component.igNode = 'node1';
      mockGroupService.deleteInterestGroupAsync.mockResolvedValue(undefined);

      await component.deleteGroup();

      expect(mockGroupService.deleteInterestGroupAsync).toHaveBeenCalledWith({
        id: 'node1',
        purgedata: true,
        purgelogs: true,
      });
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/explore']);
      expect(component.deleting()).toBe(false);
    });
  });

  describe('onCancelClick', () => {
    it('should navigate to parent when no groupId input', () => {
      component.onCancelClick();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['..']);
    });
  });
});
