import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import { PagedShares, Share, SpaceService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ManageSpaceSharingComponent } from './manage-space-sharing.component';

const mockShares: Share[] = [
  { igId: 'ig1', igName: 'Group 1', permission: 'read' },
  { igId: 'ig2', igName: 'Group 2', permission: 'write' },
];

const mockPagedShares: PagedShares = { data: mockShares, total: 2 };

describe('ManageSpaceSharingComponent', () => {
  let component: ManageSpaceSharingComponent;

  const mockSpaceService = {
    getShareSpacesAsync: vi.fn().mockResolvedValue(mockPagedShares),
    deleteShareSpace: vi.fn().mockReturnValue(of(undefined)),
    deleteShareSpaceAsync: vi.fn().mockResolvedValue(undefined),
    getShareIGsAndPermissions: vi
      .fn()
      .mockReturnValue(of({ igs: [], permissions: [] })),
  };

  const routeParams$ = of({ nodeId: 'space123' });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManageSpaceSharingComponent],
      providers: [
        { provide: SpaceService, useValue: mockSpaceService },
        { provide: ActivatedRoute, useValue: { params: routeParams$ } },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ManageSpaceSharingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load shares on init', async () => {
    await TestBed.inject(ActivatedRoute).params.toPromise();
    expect(mockSpaceService.getShareSpacesAsync).toHaveBeenCalledWith({
      id: 'space123',
      limit: 10,
      page: 1,
    });
    expect(component.shares()).toEqual(mockShares);
    expect(component.totalItems()).toBe(2);
  });

  it('should go to page and reload shares', async () => {
    await component.goToPage(3);
    expect(component.listingOptions.page).toBe(3);
    expect(mockSpaceService.getShareSpacesAsync).toHaveBeenCalledWith({
      id: 'space123',
      limit: 10,
      page: 3,
    });
  });

  it('should change limit and reset page to 1', async () => {
    await component.changeLimit(25);
    expect(component.listingOptions.limit).toBe(25);
    expect(component.listingOptions.page).toBe(1);
    expect(mockSpaceService.getShareSpacesAsync).toHaveBeenCalledWith({
      id: 'space123',
      limit: 25,
      page: 1,
    });
  });

  it('should show share space modal on invite', () => {
    component.inviteIGToShareSpace();
    expect(component.showShareSpaceModal).toBe(true);
  });

  it('should refresh and reload shares on success', async () => {
    component.showShareSpaceModal = true;
    await component.refresh({ result: ActionResult.SUCCEED });
    expect(component.showShareSpaceModal).toBe(false);
    expect(mockSpaceService.getShareSpacesAsync).toHaveBeenCalled();
  });

  it('should close modal but not reload on cancel', async () => {
    mockSpaceService.getShareSpacesAsync.mockClear();
    component.showShareSpaceModal = true;
    await component.refresh({ result: ActionResult.CANCELED });
    expect(component.showShareSpaceModal).toBe(false);
    expect(mockSpaceService.getShareSpacesAsync).not.toHaveBeenCalled();
  });

  it('should remove share and reload', async () => {
    const share: Share = { igId: 'ig1', igName: 'Group 1', permission: 'read' };
    await component.removeShare(share);
    expect(mockSpaceService.deleteShareSpaceAsync).toHaveBeenCalledWith({
      id: 'space123',
      sharedIGId: 'ig1',
    });
    expect(mockSpaceService.getShareSpacesAsync).toHaveBeenCalled();
  });
});
