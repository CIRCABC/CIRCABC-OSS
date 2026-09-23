import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco } from '@jsverse/transloco';
import {
  AutoUploadConfiguration,
  AutoUploadService,
  FTPService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { AutoUploadLibraryComponent } from './auto-upload-library.component';

class MockLoader {
  getTranslation() {
    return of({});
  }
}

describe('AutoUploadLibraryComponent', () => {
  let component: AutoUploadLibraryComponent;
  let fixture: ComponentFixture<AutoUploadLibraryComponent>;
  let mockAutoUploadService: {
    getAutoUploadEntryAsync: ReturnType<typeof vi.fn>;
    postAutoUploadEntry: ReturnType<typeof vi.fn>;
    putAutoUploadEntry: ReturnType<typeof vi.fn>;
    postAutoUploadEntryAsync: ReturnType<typeof vi.fn>;
    putAutoUploadEntryAsync: ReturnType<typeof vi.fn>;
  };
  let mockFtpService: {
    testFTPConnectionOnServerAsync: ReturnType<typeof vi.fn>;
  };
  let mockUiMessageService: { addSuccessMessage: ReturnType<typeof vi.fn> };
  let paramsSubject: Subject<{ id: string; nodeId: string }>;

  const mockConfig: AutoUploadConfiguration = {
    idConfiguration: 1,
    status: 0,
    igName: 'ig1',
    fileId: 'node1',
    ftpHost: 'ftp.example.com',
    ftpPort: 21,
    ftpUsername: 'user',
    ftpPassword: 'pass',
    ftpPath: '/path',
    dayChoice: 1,
    hourChoice: 12,
    autoExtract: true,
    jobNotifications: false,
    emails: 'a@b.com,c@d.com',
  };

  beforeEach(async () => {
    paramsSubject = new Subject();
    mockAutoUploadService = {
      getAutoUploadEntryAsync: vi.fn().mockResolvedValue(mockConfig),
      postAutoUploadEntry: vi.fn().mockReturnValue(of(null)),
      putAutoUploadEntry: vi.fn().mockReturnValue(of(null)),
      postAutoUploadEntryAsync: vi.fn().mockResolvedValue(null),
      putAutoUploadEntryAsync: vi.fn().mockResolvedValue(null),
    };
    mockFtpService = {
      testFTPConnectionOnServerAsync: vi.fn().mockResolvedValue({ code: 200 }),
    };
    mockUiMessageService = { addSuccessMessage: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [AutoUploadLibraryComponent],
      providers: [
        { provide: AutoUploadService, useValue: mockAutoUploadService },
        { provide: FTPService, useValue: mockFtpService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
          loader: MockLoader,
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AutoUploadLibraryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load configuration when route params emit', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await fixture.whenStable();

    expect(mockAutoUploadService.getAutoUploadEntryAsync).toHaveBeenCalledWith({
      id: 'ig1',
      nodeId: 'node1',
    });
    expect(component.autoUploadForm.controls['ftpHost'].value).toBe(
      'ftp.example.com'
    );
    expect(component.autoUploadForm.controls['ftpPort'].value).toBe(21);
    expect(component.autoUploadForm.controls['autoExtractZip'].value).toBe(
      true
    );
  });

  it('should parse emails replacing commas with newlines', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await fixture.whenStable();

    expect(component.autoUploadForm.controls['emailRecipients'].value).toBe(
      'a@b.com\nc@d.com'
    );
  });

  it('should test FTP connection and set connectionResult', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await fixture.whenStable();

    await component.testConnection();

    expect(mockFtpService.testFTPConnectionOnServerAsync).toHaveBeenCalled();
    expect(component.connectionResult()).toBe(200);
  });

  it('should not save when connectionResult is 0', async () => {
    component.connectionResult.set(0);
    component.configuration.set(mockConfig);

    await component.save('add');

    expect(
      mockAutoUploadService.postAutoUploadEntryAsync
    ).not.toHaveBeenCalled();
  });

  it('should save configuration when connectionResult > 0', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await fixture.whenStable();

    component.connectionResult.set(200);
    component.autoUploadForm.controls['ftpHost'].setValue('ftp.new.com');
    component.autoUploadForm.controls['ftpPort'].setValue('22');
    component.autoUploadForm.controls['emailRecipients'].setValue('x@y.com');

    await component.save('add');

    expect(mockAutoUploadService.postAutoUploadEntryAsync).toHaveBeenCalledWith(
      {
        id: 'ig1',
        autoUploadConfiguration: expect.objectContaining({
          ftpHost: 'ftp.new.com',
          ftpPort: 22,
        }),
      }
    );
  });

  it('should toggle configuration status', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await fixture.whenStable();

    await component.toggleConfiguration();

    expect(mockAutoUploadService.putAutoUploadEntryAsync).toHaveBeenCalledWith({
      id: 'ig1',
      configurationId: '1',
      enable: true,
    });
  });

  it('should not toggle when status is 2', async () => {
    mockAutoUploadService.getAutoUploadEntryAsync.mockResolvedValue({
      ...mockConfig,
      status: 2,
    });
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await fixture.whenStable();

    await component.toggleConfiguration();

    expect(
      mockAutoUploadService.putAutoUploadEntryAsync
    ).not.toHaveBeenCalled();
  });

  it('should reset form and reload configuration', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await fixture.whenStable();
    component.connectionResult.set(200);

    await component.resetForm();

    expect(component.connectionResult()).toBe(0);
    expect(mockAutoUploadService.getAutoUploadEntryAsync).toHaveBeenCalledTimes(
      2
    );
  });

  it('should report isUpdate true when idConfiguration is defined', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await fixture.whenStable();

    expect(component.isUpdate).toBe(true);
  });

  it('should report isUpdate false when configuration has no idConfiguration', async () => {
    mockAutoUploadService.getAutoUploadEntryAsync.mockResolvedValue({});
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await fixture.whenStable();

    expect(component.isUpdate).toBe(false);
  });
});
