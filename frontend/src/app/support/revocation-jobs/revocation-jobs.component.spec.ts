import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  HistoryService,
  PagedUserRevocationRequest,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { RevocationJobsComponent } from './revocation-jobs.component';

const mockRevocations: PagedUserRevocationRequest = {
  data: [],
  total: 25,
};

const mockHistoryService = {
  getRevocationsAsync: vi.fn().mockResolvedValue(mockRevocations),
};

describe('RevocationJobsComponent', () => {
  let component: RevocationJobsComponent;
  let fixture: ComponentFixture<RevocationJobsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RevocationJobsComponent],
      providers: [
        { provide: HistoryService, useValue: mockHistoryService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    fixture = TestBed.createComponent(RevocationJobsComponent);
    component = fixture.componentInstance;
    mockHistoryService.getRevocationsAsync.mockClear();
    mockHistoryService.getRevocationsAsync.mockResolvedValue(mockRevocations);
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load revocations on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockHistoryService.getRevocationsAsync).toHaveBeenCalledWith({
      limit: 10,
      page: 1,
      filter: '',
    });
    expect(component.revocations()).toEqual(mockRevocations);
    expect(component.totalItems()).toBe(25);
    expect(component.loading()).toBe(false);
  });

  it('should handle error on init', async () => {
    mockHistoryService.getRevocationsAsync.mockRejectedValue(new Error('fail'));
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.loading()).toBe(false);
    expect(spy).toHaveBeenCalled();
    spy.mockRestore();
  });

  it('should go to page', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.goToPage(3);
    await fixture.whenStable();

    expect(component.listingOptions().page).toBe(3);
    expect(mockHistoryService.getRevocationsAsync).toHaveBeenCalledWith({
      limit: 10,
      page: 3,
      filter: '',
    });
  });

  it('should change limit and reset page to 0', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.changeLimit(25);
    await fixture.whenStable();

    expect(component.listingOptions().limit).toBe(25);
    expect(component.listingOptions().page).toBe(0);
    expect(mockHistoryService.getRevocationsAsync).toHaveBeenCalledWith({
      limit: 25,
      page: 0,
      filter: '',
    });
  });

  it('should change page with custom listing options', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.changePage({ page: 2, limit: 5, sort: 'approved' });
    await fixture.whenStable();

    expect(mockHistoryService.getRevocationsAsync).toHaveBeenCalledWith({
      limit: 5,
      page: 2,
      filter: 'approved',
    });
    expect(component.totalItems()).toBe(25);
  });

  it('should not update totalItems when total is undefined', async () => {
    mockHistoryService.getRevocationsAsync.mockResolvedValue({
      data: [],
      total: undefined,
    });

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.totalItems()).toBe(0);
  });
});
