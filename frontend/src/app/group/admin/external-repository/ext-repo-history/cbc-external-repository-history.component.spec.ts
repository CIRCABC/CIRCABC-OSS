import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { ExternalRepositoryData } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ExternalRepositoryHistoryComponent } from './cbc-external-repository-history.component';

describe('ExternalRepositoryHistoryComponent', () => {
  const paramsSubject = new Subject<{ id?: string }>();

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({ properties: { domain: 'internal' } }),
  };

  const mockAresBridgeHelperService = {
    groupLog: vi.fn().mockResolvedValue([]),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExternalRepositoryHistoryComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: LoginService, useValue: mockLoginService },
        {
          provide: AresBridgeHelperService,
          useValue: mockAresBridgeHelperService,
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(ExternalRepositoryHistoryComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should set isExternalUser to true when domain is external', async () => {
    mockLoginService.getUser.mockReturnValue({
      properties: { domain: 'external' },
    });
    const fixture = TestBed.createComponent(ExternalRepositoryHistoryComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.isExternalUser()).toBe(true);
  });

  it('should set isExternalUser to false when domain is not external', async () => {
    mockLoginService.getUser.mockReturnValue({
      properties: { domain: 'internal' },
    });
    const fixture = TestBed.createComponent(ExternalRepositoryHistoryComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.isExternalUser()).toBe(false);
  });

  it('should load group log when route params contain id', async () => {
    const mockData: ExternalRepositoryData[] = [
      {
        nodeId: 'n1',
        nodeName: 'file.pdf',
        versionLabel: '1.0',
        transactionId: 'tx1',
        documentId: 'doc1',
      },
    ];
    mockAresBridgeHelperService.groupLog.mockResolvedValue(mockData);

    const fixture = TestBed.createComponent(ExternalRepositoryHistoryComponent);
    fixture.detectChanges();
    paramsSubject.next({ id: 'group123' });
    await fixture.whenStable();

    expect(mockAresBridgeHelperService.groupLog).toHaveBeenCalledWith(
      'group123'
    );
    expect(fixture.componentInstance.groupLog()).toEqual(mockData);
  });

  it('should not load group log when route params have no id', async () => {
    const fixture = TestBed.createComponent(ExternalRepositoryHistoryComponent);
    fixture.detectChanges();
    paramsSubject.next({});
    await fixture.whenStable();

    expect(mockAresBridgeHelperService.groupLog).not.toHaveBeenCalled();
  });

  it('should return correct ares document link', () => {
    const fixture = TestBed.createComponent(ExternalRepositoryHistoryComponent);
    const link = fixture.componentInstance.aresDocumentLink('doc123');
    expect(link).toBe(
      `${environment.aresBridgeServer}/Ares/document/show.do?documentId=doc123`
    );
  });

  it('should return correct circabc document link', async () => {
    mockAresBridgeHelperService.groupLog.mockResolvedValue([]);
    const fixture = TestBed.createComponent(ExternalRepositoryHistoryComponent);
    fixture.detectChanges();
    paramsSubject.next({ id: 'grp1' });
    await fixture.whenStable();

    expect(mockAresBridgeHelperService.groupLog).toHaveBeenCalled();

    const link = fixture.componentInstance.circabcDocumentLink('node456');
    const expectedBase = `${environment.serverURL}${environment.baseHref.substring(1)}`;
    expect(link).toBe(`${expectedBase}group/grp1/library/node456/details`);
  });
});
