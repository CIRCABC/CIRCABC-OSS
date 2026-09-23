import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { CategoryService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { GroupRequestsDeleteComponent } from './group-requests-delete.component';

const mockRequests = {
  data: [
    {
      id: 1,
      from: { userId: 'user1' },
      justification: 'No longer needed',
    },
  ],
  total: 1,
};

const mockCategoryService = {
  getGroupDeletionRequestsAsync: vi.fn().mockResolvedValue(mockRequests),
};

const mockRoute = {
  params: of({ id: 'cat-123' }),
};

describe('GroupRequestsDeleteComponent', () => {
  let component: GroupRequestsDeleteComponent;
  let fixture: ComponentFixture<GroupRequestsDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupRequestsDeleteComponent],
      providers: [
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

    mockCategoryService.getGroupDeletionRequestsAsync.mockClear();
    mockCategoryService.getGroupDeletionRequestsAsync.mockResolvedValue(
      mockRequests
    );

    fixture = TestBed.createComponent(GroupRequestsDeleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set categoryId from route params and load requests', () => {
    expect(component.categoryId()).toBe('cat-123');
    expect(
      mockCategoryService.getGroupDeletionRequestsAsync
    ).toHaveBeenCalledWith({
      id: 'cat-123',
      limit: 5,
      page: 1,
      filter: 'waiting',
    });
    expect(component.requests()).toEqual(mockRequests);
  });

  it('should change status on loadRequests with tab index', async () => {
    component.loadRequests({ index: 1 });
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.status()).toBe('approved');
    expect(component.page()).toBe(1);

    component.loadRequests({ index: 2 });
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.status()).toBe('rejected');

    component.loadRequests({ index: 0 });
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.status()).toBe('waiting');
  });

  it('should update page on goToPage', async () => {
    component.goToPage(3);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.page()).toBe(3);
    expect(
      mockCategoryService.getGroupDeletionRequestsAsync
    ).toHaveBeenCalledWith({
      id: 'cat-123',
      limit: 5,
      page: 3,
      filter: 'waiting',
    });
  });

  it('should update limit and reset page on changeLimit', async () => {
    component.goToPage(3);
    fixture.detectChanges();
    await fixture.whenStable();

    component.changeLimit(10);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.limit()).toBe(10);
    expect(component.page()).toBe(1);
  });

  it('should reset pagination on reloadGroupRequests', async () => {
    component.limit.set(10);
    component.page.set(3);
    fixture.detectChanges();
    await fixture.whenStable();

    component.reloadGroupRequests();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.limit()).toBe(5);
    expect(component.page()).toBe(1);
  });

  it('should set loading to false after requests resolve', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.loading()).toBe(false);
  });
});
