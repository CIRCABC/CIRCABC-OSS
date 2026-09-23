import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { DashboardService, UserActionLog } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { RecentConsultationComponent } from './recent-consultation.component';

const mockDownloads: UserActionLog[] = [
  {
    actionDate: '2026-01-01',
    action: 'download',
    node: { id: '1' },
    igNode: 'ig1',
    username: 'user1',
  },
  {
    actionDate: '2026-01-02',
    action: 'download',
    node: { id: '2' },
    igNode: 'ig1',
    username: 'user1',
  },
];

const mockUploads: UserActionLog[] = [
  {
    actionDate: '2026-01-03',
    action: 'upload',
    node: { id: '3' },
    igNode: 'ig2',
    username: 'user1',
  },
];

const mockDashboardService = {
  getUserDownloadsAsync: vi.fn(),
  getUserUploadsAsync: vi.fn(),
};

const mockLoginService = {
  getCurrentUsername: vi.fn(),
};

describe('RecentConsultationComponent', () => {
  let component: RecentConsultationComponent;
  let fixture: ComponentFixture<RecentConsultationComponent>;

  beforeEach(async () => {
    mockLoginService.getCurrentUsername.mockReturnValue('testuser');
    mockDashboardService.getUserDownloadsAsync.mockResolvedValue(mockDownloads);
    mockDashboardService.getUserUploadsAsync.mockResolvedValue(mockUploads);

    await TestBed.configureTestingModule({
      imports: [RecentConsultationComponent],
      providers: [
        { provide: DashboardService, useValue: mockDashboardService },
        { provide: LoginService, useValue: mockLoginService },
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RecentConsultationComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load downloads and uploads on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockDashboardService.getUserDownloadsAsync).toHaveBeenCalledWith({
      userId: 'testuser',
    });
    expect(mockDashboardService.getUserUploadsAsync).toHaveBeenCalledWith({
      userId: 'testuser',
    });
    expect(component.lastDownloads()).toEqual(mockDownloads);
    expect(component.lastUploads()).toEqual(mockUploads);
    expect(component.loadingDownloads()).toBe(false);
    expect(component.loadingUploads()).toBe(false);
  });

  it('should not load data when username is empty', async () => {
    mockLoginService.getCurrentUsername.mockReturnValue('');

    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockDashboardService.getUserDownloadsAsync).not.toHaveBeenCalled();
    expect(mockDashboardService.getUserUploadsAsync).not.toHaveBeenCalled();
  });

  it('should remove duplicate downloads based on node id', async () => {
    const duplicates: UserActionLog[] = [
      { node: { id: '1' }, action: 'download' },
      { node: { id: '1' }, action: 'download' },
      { node: { id: '2' }, action: 'download' },
    ];
    mockDashboardService.getUserDownloadsAsync.mockResolvedValue(duplicates);

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.lastDownloads()).toHaveLength(2);
  });

  it('should default showDownloads to true', () => {
    expect(component.showDownloads).toBe(true);
  });
});
