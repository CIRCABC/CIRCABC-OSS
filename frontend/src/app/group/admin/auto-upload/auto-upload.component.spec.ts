import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import {
  AutoUploadService,
  FTPService,
  InterestGroupService,
  NodesService,
  PagedAutoUploadConfiguration,
} from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { AutoUploadComponent } from './auto-upload.component';

const mockPagedResult: PagedAutoUploadConfiguration = {
  data: [
    { idConfiguration: 1, status: 0, dateRestriction: '* * 10 ? * 2' },
    { idConfiguration: 2, status: 1, dateRestriction: undefined },
  ],
  total: 2,
};

describe('AutoUploadComponent', () => {
  let component: AutoUploadComponent;
  let fixture: ComponentFixture<AutoUploadComponent>;
  let paramsSubject: Subject<{ [key: string]: string }>;

  const mockAutoUploadService = {
    getAutoUploadEntriesAsync: vi.fn().mockResolvedValue(mockPagedResult),
    deleteAutoUploadEntryAsync: vi.fn().mockResolvedValue(undefined),
    putAutoUploadEntryAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockTranslocoService = {
    translate: vi.fn((key: string) => key),
    config: { reRenderOnLangChange: false, defaultLang: 'en' },
    langChanges$: of('en'),
    getActiveLang: vi.fn().mockReturnValue('en'),
    selectTranslation: vi.fn().mockReturnValue(of({})),
    selectTranslate: vi.fn().mockReturnValue(of('')),
    load: vi.fn().mockReturnValue(of({})),
    _loadDependencies: vi.fn().mockReturnValue(of([])),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [AutoUploadComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: AutoUploadService, useValue: mockAutoUploadService },
        {
          provide: InterestGroupService,
          useValue: {
            getInterestGroup: vi
              .fn()
              .mockReturnValue(of({ libraryId: 'lib-1' })),
          },
        },
        {
          provide: NodesService,
          useValue: { getPath: vi.fn().mockReturnValue(of([])) },
        },
        {
          provide: FTPService,
          useValue: { testFTPConnection: vi.fn().mockReturnValue(of(0)) },
        },
        { provide: TranslocoService, useValue: mockTranslocoService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(AutoUploadComponent, {
        add: { schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(AutoUploadComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load configurations on route params change', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    expect(component.igId()).toBe('ig-123');
    expect(
      mockAutoUploadService.getAutoUploadEntriesAsync
    ).toHaveBeenCalledWith({
      id: 'ig-123',
      limit: 10,
      page: 1,
    });
    expect(component.configurations()).toHaveLength(2);
    expect(component.totalItems()).toBe(2);
  });

  it('should change page and reload configurations', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    mockAutoUploadService.getAutoUploadEntriesAsync.mockClear();
    await component.goToPage(2);

    expect(component.listingOptions.page).toBe(2);
    expect(
      mockAutoUploadService.getAutoUploadEntriesAsync
    ).toHaveBeenCalledWith({
      id: 'ig-123',
      limit: 10,
      page: 2,
    });
  });

  it('should change limit and reset page to 1', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    mockAutoUploadService.getAutoUploadEntriesAsync.mockClear();
    await component.changeLimit(25);

    expect(component.listingOptions.limit).toBe(25);
    expect(component.listingOptions.page).toBe(1);
    expect(
      mockAutoUploadService.getAutoUploadEntriesAsync
    ).toHaveBeenCalledWith({
      id: 'ig-123',
      limit: 25,
      page: 1,
    });
  });

  it('should change sort and reload', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    mockAutoUploadService.getAutoUploadEntriesAsync.mockClear();
    await component.changeSort('fileName');

    expect(component.listingOptions.sort).toBe('fileName_ASC');
    expect(mockAutoUploadService.getAutoUploadEntriesAsync).toHaveBeenCalled();
  });

  it('should delete a configuration and reload', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    mockAutoUploadService.getAutoUploadEntriesAsync.mockClear();
    await component.deleteConfiguration({ idConfiguration: 1, status: 0 });

    expect(
      mockAutoUploadService.deleteAutoUploadEntryAsync
    ).toHaveBeenCalledWith({
      id: 'ig-123',
      configurationId: '1',
    });
    expect(mockAutoUploadService.getAutoUploadEntriesAsync).toHaveBeenCalled();
  });

  it('should not delete if idConfiguration is undefined', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    mockAutoUploadService.deleteAutoUploadEntryAsync.mockClear();
    await component.deleteConfiguration({ status: 0 });

    expect(
      mockAutoUploadService.deleteAutoUploadEntryAsync
    ).not.toHaveBeenCalled();
  });

  it('should toggle configuration status', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    await component.toggleConfiguration({ idConfiguration: 5, status: 0 });

    expect(mockAutoUploadService.putAutoUploadEntryAsync).toHaveBeenCalledWith({
      id: 'ig-123',
      configurationId: '5',
      enable: true,
    });
  });

  it('should not toggle if status is 2', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    mockAutoUploadService.putAutoUploadEntryAsync.mockClear();
    await component.toggleConfiguration({ idConfiguration: 5, status: 2 });

    expect(
      mockAutoUploadService.putAutoUploadEntryAsync
    ).not.toHaveBeenCalled();
  });

  it('should hide add modal and reload on configurationAdded', async () => {
    component.showAddModal = true;
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    mockAutoUploadService.getAutoUploadEntriesAsync.mockClear();
    await component.configurationAdded();

    expect(component.showAddModal).toBe(false);
    expect(mockAutoUploadService.getAutoUploadEntriesAsync).toHaveBeenCalled();
  });

  describe('prettyPrintDateRestriction', () => {
    it('should return empty array if dateRestriction is undefined', () => {
      expect(component.prettyPrintDateRestriction({})).toEqual([]);
    });

    it('should parse cron-like dateRestriction', () => {
      const result = component.prettyPrintDateRestriction({
        dateRestriction: '* * 14 ? * 3',
      });
      expect(result).toEqual(['14', '3']);
    });

    it('should return raw value if pattern does not match', () => {
      const result = component.prettyPrintDateRestriction({
        dateRestriction: 'invalid',
      });
      expect(result).toEqual(['invalid']);
    });

    it('should handle wildcard hour', () => {
      const result = component.prettyPrintDateRestriction({
        dateRestriction: '* * * ? * 5',
      });
      expect(result).toEqual(['*', '5']);
    });
  });
});
