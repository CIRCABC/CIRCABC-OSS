import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  ExternalRepositoryService,
  RepositoryConfiguration,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ExternalRepositoryPropertiesComponent } from './external-repository-properties.component';

function flush(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

describe('ExternalRepositoryPropertiesComponent', () => {
  let component: ExternalRepositoryPropertiesComponent;
  let fixture: ComponentFixture<ExternalRepositoryPropertiesComponent>;
  let paramsSubject: Subject<{ id: string }>;

  const mockLoginService = {
    getUser: vi.fn(),
  };

  const mockExternalRepoService = {
    getAvailableExternalRepositoriesAsync: vi.fn(),
    getExternalRepositoriesAsync: vi.fn(),
    addExternalRepositoriesAsync: vi.fn(),
    deleteExternalRepositoryAsync: vi.fn(),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();

    mockLoginService.getUser.mockReturnValue({
      userId: 'user1',
      properties: { domain: 'internal' },
    });
    mockExternalRepoService.getAvailableExternalRepositoriesAsync.mockResolvedValue(
      ['RepoA', 'RepoB']
    );
    mockExternalRepoService.getExternalRepositoriesAsync.mockResolvedValue([
      { name: 'RepoA', registrationDate: '2024-01-01T00:00:00Z' },
    ] as RepositoryConfiguration[]);
    mockExternalRepoService.addExternalRepositoriesAsync.mockResolvedValue({});
    mockExternalRepoService.deleteExternalRepositoryAsync.mockResolvedValue(
      undefined
    );

    await TestBed.configureTestingModule({
      imports: [ExternalRepositoryPropertiesComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        {
          provide: ExternalRepositoryService,
          useValue: mockExternalRepoService,
        },
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
    }).compileComponents();

    fixture = TestBed.createComponent(ExternalRepositoryPropertiesComponent);
    component = fixture.componentInstance;
    fixture.autoDetectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load repos when route params emit', async () => {
    paramsSubject.next({ id: 'ig123' });
    await fixture.whenStable();

    expect(component.igId()).toBe('ig123');
    expect(component.reposFormArray).toHaveLength(2);
    expect(component.reposFormArray.at(0).value.name).toBe('RepoA');
    expect(component.reposFormArray.at(0).value.enabled).toBe(true);
    expect(component.reposFormArray.at(1).value.name).toBe('RepoB');
    expect(component.reposFormArray.at(1).value.enabled).toBe(false);
  });

  it('should set isExternalUser to true when domain is external', async () => {
    mockLoginService.getUser.mockReturnValue({
      userId: 'ext1',
      properties: { domain: 'external' },
    });

    fixture = TestBed.createComponent(ExternalRepositoryPropertiesComponent);
    component = fixture.componentInstance;
    fixture.autoDetectChanges();
    paramsSubject.next({ id: 'ig123' });
    await fixture.whenStable();

    expect(component.isExternalUser).toBe(true);
    expect(component.reposFormArray.at(0).get('enabled')?.disabled).toBe(true);
  });

  it('should call addExternalRepositories when enabling a repo', async () => {
    mockExternalRepoService.getExternalRepositoriesAsync.mockResolvedValue([]);
    paramsSubject.next({ id: 'ig123' });
    await fixture.whenStable();

    component.reposFormArray.at(0).get('enabled')?.setValue(true);

    mockExternalRepoService.getExternalRepositoriesAsync.mockResolvedValue([]);
    await component.save();
    await flush();

    expect(
      mockExternalRepoService.addExternalRepositoriesAsync
    ).toHaveBeenCalledWith({ id: 'ig123', repoId: 'RepoA' });
  });

  it('should call deleteExternalRepository when disabling a repo', async () => {
    mockExternalRepoService.getExternalRepositoriesAsync.mockResolvedValue([
      { name: 'RepoA', registrationDate: '2024-01-01T00:00:00Z' },
      { name: 'RepoB', registrationDate: '2024-02-01T00:00:00Z' },
    ] as RepositoryConfiguration[]);
    paramsSubject.next({ id: 'ig123' });
    await fixture.whenStable();

    component.reposFormArray.at(1).get('enabled')?.setValue(false);

    mockExternalRepoService.getExternalRepositoriesAsync.mockResolvedValue([
      { name: 'RepoA', registrationDate: '2024-01-01T00:00:00Z' },
      { name: 'RepoB', registrationDate: '2024-02-01T00:00:00Z' },
    ] as RepositoryConfiguration[]);
    await component.save();
    await flush();

    expect(
      mockExternalRepoService.deleteExternalRepositoryAsync
    ).toHaveBeenCalledWith({ id: 'ig123', repoId: 'RepoB' });
  });

  it('should reload repos on cancel', async () => {
    paramsSubject.next({ id: 'ig123' });
    await fixture.whenStable();

    mockExternalRepoService.getAvailableExternalRepositoriesAsync.mockClear();
    mockExternalRepoService.getExternalRepositoriesAsync.mockClear();
    mockExternalRepoService.getAvailableExternalRepositoriesAsync.mockResolvedValue(
      ['RepoA', 'RepoB']
    );
    mockExternalRepoService.getExternalRepositoriesAsync.mockResolvedValue([]);

    component.cancel();
    await fixture.whenStable();

    expect(
      mockExternalRepoService.getAvailableExternalRepositoriesAsync
    ).toHaveBeenCalled();
    expect(
      mockExternalRepoService.getExternalRepositoriesAsync
    ).toHaveBeenCalledWith({ id: 'ig123' });
  });
});
