import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { DownloadService } from 'app/core/download.service';
import { ContentService, NodesService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { OfficeService } from 'app/core/office.service';
import { UploadService } from 'app/core/upload.service';
import { Providers } from '@microsoft/mgt';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { OfficeComponent } from './office.component';

vi.mock('@microsoft/mgt', () => ({
  Providers: { globalProvider: null },
  Msal2Provider: vi.fn(),
  TemplateHelper: { setBindingSyntax: vi.fn() },
}));

vi.mock('environments/environment', () => ({
  environment: { officeClientId: '' },
}));

describe('OfficeComponent', () => {
  const mockRoute = {
    queryParamMap: of(
      new Map([
        ['id', '123'],
        ['mode', 'edit'],
      ])
    ),
  };
  const mockDownloadService = { getNodeContent: vi.fn() };
  const mockOfficeService = {
    rootFolderExists: vi.fn(),
    createFolder: vi.fn(),
    getFileContent: vi.fn(),
    largeFileUpload: vi.fn(),
    deleteFile: vi.fn(),
  };
  const mockContentService = {
    putCheckinAsync: vi.fn().mockResolvedValue(undefined),
    postCheckoutAsync: vi.fn().mockResolvedValue({ id: 'wc-1' }),
  };
  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue({ id: '123', name: 'test.docx' }),
  };
  const mockUploadService = { updateExistingFileContent: vi.fn() };
  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('en'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OfficeComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: DownloadService, useValue: mockDownloadService },
        { provide: OfficeService, useValue: mockOfficeService },
        { provide: ContentService, useValue: mockContentService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: UploadService, useValue: mockUploadService },
        { provide: LoginService, useValue: mockLoginService },
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

  it('should create the component', () => {
    const fixture = TestBed.createComponent(OfficeComponent);
    const component = fixture.componentInstance;
    expect(component).toBeDefined();
  });

  it('should initialize with isLogged false and spinnerMode determinate', () => {
    const fixture = TestBed.createComponent(OfficeComponent);
    const component = fixture.componentInstance;
    expect(component.isLogged).toBe(false);
    expect(component.spinnerMode()).toBe('determinate');
  });

  it('should set spinnerMode to determinate on ngOnInit when office integration is disabled', async () => {
    const fixture = TestBed.createComponent(OfficeComponent);
    const component = fixture.componentInstance;
    await component.initialize();
    expect(component.spinnerMode()).toBe('determinate');
  });

  it('should set isLogged to true when onLogin is called', () => {
    const fixture = TestBed.createComponent(OfficeComponent);
    const component = fixture.componentInstance;
    // globalProvider is null so init() won't proceed
    (Providers as { globalProvider: unknown }).globalProvider = null;
    component.onLogin();
    expect(component.isLogged).toBe(true);
  });

  it('should set active language from loginService on ngOnInit', async () => {
    const fixture = TestBed.createComponent(OfficeComponent);
    const component = fixture.componentInstance;
    const transloco = TestBed.inject(TranslocoService);
    const spy = vi.spyOn(transloco, 'setActiveLang');
    await component.initialize();
    expect(spy).toHaveBeenCalledWith('en');
  });
});
