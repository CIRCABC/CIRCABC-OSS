import { TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  AuditActivity,
  AuditService,
  BASE_PATH,
  LogSearchResult,
  MembersService,
  PagedUserProfile,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { LogComponent } from './log.component';

const mockActivities: AuditActivity[] = [
  { id: 1, name: 'Upload', service: 'Library' },
  { id: 2, name: 'Download', service: 'Library' },
  { id: 3, name: 'Create', service: 'Forum' },
];

const mockPagedUserProfile: PagedUserProfile = {
  data: [
    {
      user: { userId: 'user1', firstname: 'John', lastname: 'Doe' },
    },
    {
      user: { userId: 'user2', firstname: 'Jane', lastname: 'Smith' },
    },
  ],
  total: 2,
};

const mockSearchResults: LogSearchResult[] = [
  {
    logDate: '2026-04-20T10:00:00Z',
    activityDescription: 'Upload',
    serviceDescription: 'Library',
    userId: 'user1',
    information: 'file.txt',
    path: '/group/library',
    success: true,
  },
];

describe('LogComponent', () => {
  let component: LogComponent;
  const paramsSubject = new Subject<{ id: string }>();

  const mockMembersService = {
    getMembersAsync: vi.fn().mockResolvedValue(mockPagedUserProfile),
  };
  const mockAuditService = {
    getAllAuditActivitiesAsync: vi.fn().mockResolvedValue(mockActivities),
    getAuditsAsync: vi.fn().mockResolvedValue(mockSearchResults),
  };
  const mockSaveAsService = {
    saveUrlAs: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LogComponent],
      providers: [
        provideNativeDateAdapter(),
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: MembersService, useValue: mockMembersService },
        { provide: AuditService, useValue: mockAuditService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: BASE_PATH, useValue: 'http://localhost' },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(LogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form with default values', () => {
    expect(component.form).toBeDefined();
    expect(component.form.controls['users'].value).toBe('');
    expect(component.form.controls['services'].value).toBe('');
    expect(component.form.controls['activities'].value).toBe('');
    expect(component.form.controls['dateFrom'].value).toBeInstanceOf(Date);
    expect(component.form.controls['dateTo'].value).toBeInstanceOf(Date);
  });

  it('should load users and activities when route params emit', async () => {
    paramsSubject.next({ id: 'group123' });
    await vi.waitFor(() => {
      expect(component.preloading()).toBe(false);
    });

    expect(component.groupId).toBe('group123');
    expect(mockMembersService.getMembersAsync).toHaveBeenCalledWith({
      id: 'group123',
      searchQuery: '',
    });
    expect(mockAuditService.getAllAuditActivitiesAsync).toHaveBeenCalledWith({
      id: 'group123',
    });
    expect(component.users()!).toHaveLength(3); // 2 users + 'All'
    expect(component.users()![0]).toEqual({ id: '', name: 'All' });
    expect(component.services()!).toHaveLength(3); // Forum, Library + 'All'
    expect(component.activities()).toHaveLength(4); // Create, Download, Upload + 'All'
  });

  it('should filter activities by service on serviceClick', async () => {
    paramsSubject.next({ id: 'group123' });
    await vi.waitFor(() => {
      expect(component.preloading()).toBe(false);
    });

    component.form.controls['services'].setValue('Library');
    component.serviceClick('Library');

    expect(component.activities()).toHaveLength(3); // Download, Upload + 'All'
    expect(component.activities().map((a) => a.name)).toContain('Upload');
    expect(component.activities().map((a) => a.name)).toContain('Download');
    expect(component.activities().map((a) => a.name)).not.toContain('Create');
    expect(component.form.controls['activities'].value).toBe('');
  });

  it('should call auditService.getAudits on search when form is valid', async () => {
    paramsSubject.next({ id: 'group123' });
    await vi.waitFor(() => {
      expect(component.preloading()).toBe(false);
    });

    await component.search();

    expect(mockAuditService.getAuditsAsync).toHaveBeenCalled();
    expect(component.searchResults()).toEqual(mockSearchResults);
    expect(component.loading()).toBe(false);
  });

  it('should not call auditService.getAudits on search when form is invalid', async () => {
    paramsSubject.next({ id: 'group123' });
    await vi.waitFor(() => {
      expect(component.preloading()).toBe(false);
    });

    mockAuditService.getAuditsAsync.mockClear();
    component.form.controls['dateFrom'].setValue(null);

    await component.search();

    expect(mockAuditService.getAuditsAsync).not.toHaveBeenCalled();
  });

  it('should call saveAsService.saveUrlAs on export', async () => {
    paramsSubject.next({ id: 'group123' });
    await vi.waitFor(() => {
      expect(component.preloading()).toBe(false);
    });

    component.export('csv');

    expect(mockSaveAsService.saveUrlAs).toHaveBeenCalled();
    const url = mockSaveAsService.saveUrlAs.mock.calls[0][0] as string;
    expect(url).toContain('http://localhost/audit/group123');
    expect(url).toContain('format=csv');
    expect(mockSaveAsService.saveUrlAs.mock.calls[0][1]).toBe('AuditLog.csv');
  });
});
