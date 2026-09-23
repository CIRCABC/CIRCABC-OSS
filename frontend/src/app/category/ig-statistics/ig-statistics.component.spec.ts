import { NO_ERRORS_SCHEMA } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  CategoryService,
  PagedStatisticsContents,
  StatisticsContent,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { ALF_BASE_PATH } from 'app/core/variables';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { IgStatisticsComponent } from './ig-statistics.component';

const mockStatisticsData: PagedStatisticsContents = {
  data: [
    {
      name: 'report.xlsx',
      size: '1024',
      modifiedDate: '2026-01-01',
      downloadURL:
        '/d/d/workspace/SpacesStore/abc-def-123-456-789-012-345-678-901-23/report.xlsx',
    },
  ],
  total: 1,
};

describe('IgStatisticsComponent', () => {
  let component: IgStatisticsComponent;
  let fixture: ComponentFixture<IgStatisticsComponent>;
  let paramsSubject: Subject<{ [key: string]: string }>;

  const mockCategoryService = {
    getCategoryStatisticsAsync: vi.fn().mockResolvedValue(mockStatisticsData),
    postCategoryStatistics: vi.fn().mockReturnValue(of(undefined)),
    postCategoryStatisticsAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockSaveAsService = {
    saveUrlAs: vi.fn(),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [IgStatisticsComponent],
      providers: [
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: ALF_BASE_PATH, useValue: 'http://localhost/alfresco' },
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
    })
      .overrideComponent(IgStatisticsComponent, {
        set: {
          imports: [TranslocoModule, DatePipe],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(IgStatisticsComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    component.ngOnDestroy();
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load contents when route params emit', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat-1' });
    await new Promise((resolve) => setTimeout(resolve, 0));

    expect(component.categoryId).toBe('cat-1');
    expect(component.contents()).toEqual(mockStatisticsData.data);
    expect(component.totalItems()).toBe(1);
    expect(component.loading()).toBe(false);
    expect(mockCategoryService.getCategoryStatisticsAsync).toHaveBeenCalledWith(
      {
        id: 'cat-1',
        limit: 5,
        page: 1,
      }
    );
  });

  it('should set up a polling interval after route params emit', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat-1' });
    // Allow the async subscribe callback to complete
    await new Promise((resolve) => setTimeout(resolve, 0));

    expect(
      (component as unknown as { interval: unknown }).interval
    ).toBeDefined();
  });

  it('should change page and reload', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat-1' });
    await new Promise((resolve) => setTimeout(resolve, 0));

    mockCategoryService.getCategoryStatisticsAsync.mockClear();
    await component.goToPage(2);

    expect(component.listingOptions.page).toBe(2);
    expect(mockCategoryService.getCategoryStatisticsAsync).toHaveBeenCalledWith(
      {
        id: 'cat-1',
        limit: 5,
        page: 2,
      }
    );
  });

  it('should call postCategoryStatistics on generateStatistics', async () => {
    component.categoryId = 'cat-1';
    await component.generateStatistics();

    expect(
      mockCategoryService.postCategoryStatisticsAsync
    ).toHaveBeenCalledWith({ id: 'cat-1' });
  });

  it('should call saveUrlAs with correct URL on download', () => {
    const content: StatisticsContent = {
      name: 'report.xlsx',
      downloadURL:
        '/d/d/workspace/SpacesStore/abc-def-123-456-789-012-345-678-901-23/report.xlsx',
    };

    component.download(content);

    expect(mockSaveAsService.saveUrlAs).toHaveBeenCalledWith(
      'http://localhost/alfresco/node/content' +
        content.downloadURL?.substring(4, 63),
      'report.xlsx'
    );
  });

  it('should clear interval on destroy', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat-1' });
    await new Promise((resolve) => setTimeout(resolve, 0));

    const intervalRef = (component as unknown as { interval: unknown })
      .interval;
    const clearIntervalSpy = vi.spyOn(globalThis, 'clearInterval');
    component.ngOnDestroy();

    expect(clearIntervalSpy).toHaveBeenCalledWith(intervalRef);
  });
});
