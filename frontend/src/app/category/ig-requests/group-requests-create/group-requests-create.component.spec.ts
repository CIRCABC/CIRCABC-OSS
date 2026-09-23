import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  CategoryService,
  GroupCreationRequest,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { GroupRequestsCreateComponent } from './group-requests-create.component';

const mockRequest: GroupCreationRequest = {
  from: { userId: 'user1' },
  proposedName: 'TestGroup',
  justification: 'Need a group',
};

const mockCategoryService = {
  getInterestGroupRequestsAsync: vi
    .fn()
    .mockResolvedValue({ data: [mockRequest], total: 1 }),
  getInterestGroupsByCategoryIdAsync: vi
    .fn()
    .mockResolvedValue([{ id: 'ig1', name: 'TestGroup' }]),
};

const mockRoute = {
  params: of({ id: 'cat1' }),
};

describe('GroupRequestsCreateComponent', () => {
  let component: GroupRequestsCreateComponent;
  let fixture: ComponentFixture<GroupRequestsCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupRequestsCreateComponent],
      providers: [
        provideRouter([]),
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: ActivatedRoute, useValue: mockRoute },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    vi.clearAllMocks();
    mockCategoryService.getInterestGroupRequestsAsync.mockResolvedValue({
      data: [mockRequest],
      total: 1,
    });
    mockCategoryService.getInterestGroupsByCategoryIdAsync.mockResolvedValue([
      { id: 'ig1', name: 'TestGroup' },
    ]);

    fixture = TestBed.createComponent(GroupRequestsCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should set categoryId from route params and load requests', () => {
    expect(component.categoryId()).toBe('cat1');
    expect(
      mockCategoryService.getInterestGroupRequestsAsync
    ).toHaveBeenCalledWith({
      id: 'cat1',
      limit: 5,
      page: 1,
      filter: 'waiting',
    });
  });

  it('should map interestGroupId when group name matches proposedName', () => {
    expect(component.requests().data[0].interestGroupId).toBe('ig1');
  });

  it('should not fetch groups when requests data is empty', async () => {
    mockCategoryService.getInterestGroupRequestsAsync.mockResolvedValue({
      data: [],
      total: 0,
    });
    vi.clearAllMocks();

    component.limit.set(10);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(
      mockCategoryService.getInterestGroupsByCategoryIdAsync
    ).not.toHaveBeenCalled();
  });

  it('should change status and reload on loadRequests', async () => {
    vi.clearAllMocks();
    component.loadRequests({ index: 1 });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.status()).toBe('approved');
    expect(component.page()).toBe(1);
    expect(
      mockCategoryService.getInterestGroupRequestsAsync
    ).toHaveBeenCalled();
  });

  it('should set status to rejected for index 2', () => {
    component.loadRequests({ index: 2 });
    expect(component.status()).toBe('rejected');
  });

  it('should update page on goToPage', async () => {
    vi.clearAllMocks();
    component.goToPage(3);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.page()).toBe(3);
    expect(
      mockCategoryService.getInterestGroupRequestsAsync
    ).toHaveBeenCalled();
  });

  it('should update limit and reset page on changeLimit', async () => {
    vi.clearAllMocks();
    component.changeLimit(10);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.limit()).toBe(10);
    expect(component.page()).toBe(1);
    expect(
      mockCategoryService.getInterestGroupRequestsAsync
    ).toHaveBeenCalled();
  });

  it('should reset pagination on reloadGroupRequests', async () => {
    component.page.set(3);
    component.limit.set(10);
    vi.clearAllMocks();
    component.reloadGroupRequests();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.page()).toBe(1);
    expect(component.limit()).toBe(5);
    expect(
      mockCategoryService.getInterestGroupRequestsAsync
    ).toHaveBeenCalled();
  });
});
