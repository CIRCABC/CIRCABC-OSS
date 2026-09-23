import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  CategoryService,
  ContentService,
  InterestGroupService,
  ProfileService,
  SpaceService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { VerifyConditionsDeleteIgComponent } from './verify-conditions-delete-ig.component';

describe('VerifyConditionsDeleteIgComponent', () => {
  let component: VerifyConditionsDeleteIgComponent;

  const mockDialogRef = { close: vi.fn() };
  const mockData = { group: { id: 'group-1' } };
  const mockGroupService = { isGroupDeletableAsync: vi.fn() };
  const mockContentService = {
    deleteCheckout: vi.fn(),
    deleteCheckoutAsync: vi.fn(),
  };
  const mockSpaceService = {
    getShareSpacesAsync: vi.fn(),
    deleteShareSpace: vi.fn(),
    deleteShareSpaceAsync: vi.fn(),
  };
  const mockProfileService = { putProfile: vi.fn(), putProfileAsync: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    mockGroupService.isGroupDeletableAsync.mockResolvedValue({
      lockedNodes: [],
      sharedNodes: [],
      sharedProfiles: [],
    });

    TestBed.configureTestingModule({
      imports: [VerifyConditionsDeleteIgComponent],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: mockData },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: CategoryService, useValue: {} },
        { provide: InterestGroupService, useValue: mockGroupService },
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
    }).overrideComponent(VerifyConditionsDeleteIgComponent, {
      set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
    });

    const fixture = TestBed.createComponent(VerifyConditionsDeleteIgComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should close dialog when all conditions are empty on init', async () => {
    await component.verifyConditions();
    expect(mockDialogRef.close).toHaveBeenCalledWith(true);
  });

  it('should not close dialog when there are locked nodes', async () => {
    mockGroupService.isGroupDeletableAsync.mockResolvedValue({
      lockedNodes: [{ id: 'node-1' }],
      sharedNodes: [],
      sharedProfiles: [],
    });
    await component.verifyConditions();
    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });

  it('should handle error in verifyConditions', async () => {
    mockGroupService.isGroupDeletableAsync.mockRejectedValue(new Error('fail'));
    await component.verifyConditions();
    expect(component.verifying).toBe(false);
    expect(component.conditions).toBeUndefined();
  });

  it('should clean locks and re-verify', async () => {
    vi.useFakeTimers();
    component.conditions = {
      lockedNodes: [{ id: 'lock-1' }],
      sharedNodes: [],
      sharedProfiles: [],
    };
    mockContentService.deleteCheckoutAsync.mockResolvedValue(undefined);

    await component.cleanLocks();

    expect(mockContentService.deleteCheckoutAsync).toHaveBeenCalledWith({
      id: 'lock-1',
    });
    expect(component.cleaningLocks).toBe(false);
    vi.useRealTimers();
  });

  it('should clean shared nodes and re-verify', async () => {
    vi.useFakeTimers();
    component.conditions = {
      lockedNodes: [],
      sharedNodes: [{ id: 'shared-1' }],
      sharedProfiles: [],
    };
    mockSpaceService.getShareSpacesAsync.mockResolvedValue({
      data: [{ igId: 'ig-1' }],
    });
    mockSpaceService.deleteShareSpaceAsync.mockResolvedValue(undefined);

    await component.cleanSharedNodes();

    expect(mockSpaceService.getShareSpacesAsync).toHaveBeenCalledWith({
      id: 'shared-1',
      limit: 0,
      page: 1,
    });
    expect(mockSpaceService.deleteShareSpaceAsync).toHaveBeenCalledWith({
      id: 'shared-1',
      sharedIGId: 'ig-1',
    });
    expect(component.cleaningSharedNodes).toBe(false);
    vi.useRealTimers();
  });

  it('should clean shared profiles and re-verify', async () => {
    vi.useFakeTimers();
    const profile = { id: 'prof-1', exported: true };
    component.conditions = {
      lockedNodes: [],
      sharedNodes: [],
      sharedProfiles: [profile],
    };
    mockProfileService.putProfileAsync.mockResolvedValue(undefined);

    await component.cleanSharedProfiles();

    expect(profile.exported).toBe(false);
    expect(mockProfileService.putProfileAsync).toHaveBeenCalledWith({
      id: 'prof-1',
      profile,
    });
    expect(component.cleaningSharedProfiles).toBe(false);
    vi.useRealTimers();
  });
});
