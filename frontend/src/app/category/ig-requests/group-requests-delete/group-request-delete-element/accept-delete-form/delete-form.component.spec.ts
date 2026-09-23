import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  CategoryService,
  ContentService,
  GroupDeletionReport,
  InterestGroupService,
  ProfileService,
  SpaceService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AcceptDeleteFormComponent } from './delete-form.component';

describe('AcceptDeleteFormComponent', () => {
  const mockDialogData = {
    request: { groupId: 'group-1', id: 42 },
    categoryId: 'cat-1',
  };

  const mockDialogRef = { close: vi.fn() };

  const mockGroupService = {
    isGroupDeletableAsync: vi.fn(),
    deleteInterestGroup: vi.fn(),
    deleteInterestGroupAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockCategoryService = {
    validateInterestGroupDeleteRequests: vi.fn(),
    validateInterestGroupDeleteRequestsAsync: vi
      .fn()
      .mockResolvedValue(undefined),
  };

  const mockContentService = {
    deleteCheckout: vi.fn(),
    deleteCheckoutAsync: vi.fn().mockResolvedValue(undefined),
  };
  const mockSpaceService = {
    getShareSpacesAsync: vi.fn(),
    deleteShareSpace: vi.fn(),
    deleteShareSpaceAsync: vi.fn().mockResolvedValue(undefined),
  };
  const mockProfileService = {
    putProfile: vi.fn(),
    putProfileAsync: vi.fn().mockResolvedValue([]),
  };

  let component: AcceptDeleteFormComponent;

  beforeEach(() => {
    vi.clearAllMocks();
    mockGroupService.isGroupDeletableAsync.mockResolvedValue({
      lockedNodes: [],
      sharedNodes: [],
      sharedProfiles: [],
    } as GroupDeletionReport);

    TestBed.configureTestingModule({
      imports: [AcceptDeleteFormComponent],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: mockDialogData },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: ContentService, useValue: mockContentService },
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: ProfileService, useValue: mockProfileService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    const fixture = TestBed.createComponent(AcceptDeleteFormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should call verifyConditions on init and set conditions', async () => {
    await component.verifyConditions();
    expect(mockGroupService.isGroupDeletableAsync).toHaveBeenCalledWith({
      id: 'group-1',
    });
    expect(component.verified()).toBe(true);
    expect(component.verifying()).toBe(false);
  });

  it('should handle error in verifyConditions', async () => {
    mockGroupService.isGroupDeletableAsync.mockRejectedValue(new Error('fail'));
    await component.verifyConditions();
    expect(component.verified()).toBe(false);
    expect(component.verifying()).toBe(false);
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
        lockedNodes: [{ id: 'n1' }],
        sharedNodes: [],
        sharedProfiles: [],
      });
      expect(component.isReadyForDeletion()).toBe(false);
    });

    it('should return false when conditions are undefined', () => {
      component.conditions.set(undefined);
      expect(component.isReadyForDeletion()).toBe(false);
    });
  });

  describe('cleanLocks', () => {
    it('should call deleteCheckout for each locked node', async () => {
      vi.useFakeTimers();
      component.conditions.set({
        lockedNodes: [{ id: 'n1' }, { id: 'n2' }],
        sharedNodes: [],
        sharedProfiles: [],
      });
      mockContentService.deleteCheckoutAsync.mockResolvedValue(undefined);
      mockGroupService.isGroupDeletableAsync.mockResolvedValue({
        lockedNodes: [],
        sharedNodes: [],
        sharedProfiles: [],
      });

      await component.cleanLocks();

      expect(mockContentService.deleteCheckoutAsync).toHaveBeenCalledWith({
        id: 'n1',
      });
      expect(mockContentService.deleteCheckoutAsync).toHaveBeenCalledWith({
        id: 'n2',
      });
      expect(component.cleaningLocks()).toBe(false);
      vi.useRealTimers();
    });
  });

  describe('cleanSharedNodes', () => {
    it('should delete share spaces for each shared node', async () => {
      vi.useFakeTimers();
      component.conditions.set({
        lockedNodes: [],
        sharedNodes: [{ id: 's1' }],
        sharedProfiles: [],
      });
      mockSpaceService.getShareSpacesAsync.mockResolvedValue({
        data: [{ igId: 'ig1' }],
      });
      mockSpaceService.deleteShareSpaceAsync.mockResolvedValue(undefined);
      mockGroupService.isGroupDeletableAsync.mockResolvedValue({
        lockedNodes: [],
        sharedNodes: [],
        sharedProfiles: [],
      });

      await component.cleanSharedNodes();

      expect(mockSpaceService.getShareSpacesAsync).toHaveBeenCalledWith({
        id: 's1',
        limit: 0,
        page: 1,
      });
      expect(mockSpaceService.deleteShareSpaceAsync).toHaveBeenCalledWith({
        id: 's1',
        sharedIGId: 'ig1',
      });
      expect(component.cleaningSharedNodes()).toBe(false);
      vi.useRealTimers();
    });
  });

  describe('cleanSharedProfiles', () => {
    it('should unexport each shared profile', async () => {
      vi.useFakeTimers();
      component.conditions.set({
        lockedNodes: [],
        sharedNodes: [],
        sharedProfiles: [{ id: 'p1', exported: true }],
      });
      mockProfileService.putProfileAsync.mockResolvedValue([]);
      mockGroupService.isGroupDeletableAsync.mockResolvedValue({
        lockedNodes: [],
        sharedNodes: [],
        sharedProfiles: [],
      });

      await component.cleanSharedProfiles();

      expect(mockProfileService.putProfileAsync).toHaveBeenCalledWith({
        id: 'p1',
        profile: {
          id: 'p1',
          exported: false,
        },
      });
      expect(component.cleaningSharedProfiles()).toBe(false);
      vi.useRealTimers();
    });
  });

  describe('updateRequestDeleteGroup', () => {
    it('should validate and delete the group then close dialog', async () => {
      mockCategoryService.validateInterestGroupDeleteRequestsAsync.mockResolvedValue(
        undefined
      );
      mockGroupService.deleteInterestGroupAsync.mockResolvedValue(undefined);

      await component.updateRequestDeleteGroup();

      expect(
        mockCategoryService.validateInterestGroupDeleteRequestsAsync
      ).toHaveBeenCalledWith({
        id: 'cat-1',
        groupDeletionRequestApproval: {
          id: 42,
          agreement: 1,
          argument: '',
        },
      });
      expect(mockGroupService.deleteInterestGroupAsync).toHaveBeenCalledWith({
        id: 'group-1',
        purgedata: true,
        purgelogs: true,
      });
      expect(component.deleting()).toBe(false);
      expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    });
  });
});
