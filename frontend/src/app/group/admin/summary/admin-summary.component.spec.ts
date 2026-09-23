import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  BASE_PATH,
  InterestGroupService,
  NameValue,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { AdminSummaryComponent } from './admin-summary.component';

describe('AdminSummaryComponent', () => {
  let component: AdminSummaryComponent;
  let fixture: ComponentFixture<AdminSummaryComponent>;
  let paramsSubject: Subject<{ [key: string]: string }>;

  const mockStatistics: NameValue[] = [
    { name: 'summary.statistics.created.date', value: '2024-01-01' },
    { name: 'summary.statistics.total.size', value: '100MB' },
    { name: 'summary.statistics.number.of.users', value: '42' },
  ];

  const mockTimeline = [{ action: 'created', date: '2024-01-01' }];
  const mockStructure = {
    structure: JSON.stringify({
      children: [{ name: 'Library', children: [] }],
    }),
  };

  const mockGroupService = {
    getIGSummaryStatisticsAsync: vi.fn().mockResolvedValue(mockStatistics),
    getIGSummaryTimelineAsync: vi.fn().mockResolvedValue(mockTimeline),
    getIGSummaryStructureAsync: vi.fn().mockResolvedValue(mockStructure),
  };

  const mockSaveAsService = {
    saveUrlAs: vi.fn(),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();
    vi.clearAllMocks();
    mockGroupService.getIGSummaryStatisticsAsync.mockResolvedValue(
      mockStatistics
    );

    await TestBed.configureTestingModule({
      imports: [AdminSummaryComponent],
      providers: [
        { provide: InterestGroupService, useValue: mockGroupService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: BASE_PATH, useValue: 'http://localhost:8080' },
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
      .overrideComponent(AdminSummaryComponent, {
        set: { template: '' },
      })
      .compileComponents();

    fixture = TestBed.createComponent(AdminSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  async function emitParams(params: { [key: string]: string }) {
    paramsSubject.next(params);
    fixture.detectChanges();
    await fixture.whenStable();
  }

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize forms on construction', () => {
    expect(component.summaryForm).toBeDefined();
    expect(component.exportForm).toBeDefined();
    expect(component.selectedStatistics()).toBe('1');
  });

  it('should load statistics when route params emit', async () => {
    await emitParams({ id: 'ig-123' });

    expect(component.igId()).toBe('ig-123');
    expect(mockGroupService.getIGSummaryStatisticsAsync).toHaveBeenCalledWith({
      id: 'ig-123',
      calculate: false,
    });
  });

  it('should calculate statistics if none are available', async () => {
    mockGroupService.getIGSummaryStatisticsAsync
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce(mockStatistics);

    await emitParams({ id: 'ig-456' });

    expect(mockGroupService.getIGSummaryStatisticsAsync).toHaveBeenCalledWith({
      id: 'ig-456',
      calculate: true,
    });
  });

  it('should prettify properties from statistics', async () => {
    await emitParams({ id: 'ig-123' });

    const createdDate = component.prettyProperties()[0].data[0];
    expect(createdDate.value).toBe('2024-01-01');
  });

  it('should report statisticsAvailable as true when properties exist', async () => {
    await emitParams({ id: 'ig-123' });

    expect(component.statisticsAvailable()).toBe(true);
  });

  it('should report statisticsAvailable as false when properties are empty', async () => {
    mockGroupService.getIGSummaryStatisticsAsync.mockResolvedValue([]);

    await emitParams({ id: 'ig-789' });

    expect(component.statisticsAvailable()).toBe(false);
  });

  it('should load timeline when selectedStatistics is 2', async () => {
    await emitParams({ id: 'ig-123' });

    component.summaryForm.controls['selectedStatistics'].setValue('2');
    component.changeSummary();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockGroupService.getIGSummaryTimelineAsync).toHaveBeenCalledWith({
      id: 'ig-123',
    });
    expect(component.activities()).toEqual(mockTimeline);
  });

  it('should load structure when selectedStatistics is 3', async () => {
    await emitParams({ id: 'ig-123' });

    component.summaryForm.controls['selectedStatistics'].setValue('3');
    component.changeSummary();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockGroupService.getIGSummaryStructureAsync).toHaveBeenCalledWith({
      id: 'ig-123',
    });
    expect(component.structureHolder()).toEqual(mockStructure);
  });

  it('should parse structure and return child at index', async () => {
    await emitParams({ id: 'ig-123' });

    component.summaryForm.controls['selectedStatistics'].setValue('3');
    component.changeSummary();
    fixture.detectChanges();
    await fixture.whenStable();

    const child = component.getStructure(0);
    expect(child.name).toBe('Library');
  });

  it('should call saveAsService.saveUrlAs on export', async () => {
    await emitParams({ id: 'ig-123' });
    component.exportForm.controls['export'].setValue({ code: 'csv' });

    const result = component.export('statistics', 'summary');

    expect(mockSaveAsService.saveUrlAs).toHaveBeenCalledWith(
      'http://localhost:8080/groups/ig-123/summary/export?format=csv&type=statistics',
      'summary.csv'
    );
    expect(result).toBe(false);
  });
});
