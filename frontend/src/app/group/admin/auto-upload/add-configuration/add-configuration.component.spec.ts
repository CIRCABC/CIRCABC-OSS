import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  AutoUploadService,
  FTPService,
  InterestGroupService,
  NodesService,
} from 'app/core/generated/circabc';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddConfigurationComponent } from './add-configuration.component';

const mockFtpService = {
  testFTPConnectionOnServerAsync: vi.fn().mockResolvedValue({ code: 1 }),
};

const mockAutoUploadService = {
  postAutoUploadEntryAsync: vi.fn().mockResolvedValue(undefined),
};

const mockInterestGroupService = {
  getInterestGroupAsync: vi
    .fn()
    .mockResolvedValue({ name: 'test', permissions: {}, libraryId: 'lib-123' }),
};

const mockNodesService = {
  getPathAsync: vi.fn().mockResolvedValue([]),
};

describe('AddConfigurationComponent', () => {
  let component: AddConfigurationComponent;
  let fixture: ComponentFixture<AddConfigurationComponent>;
  let componentRef: ComponentRef<AddConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddConfigurationComponent],
      providers: [
        { provide: FTPService, useValue: mockFtpService },
        { provide: AutoUploadService, useValue: mockAutoUploadService },
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: NodesService, useValue: mockNodesService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddConfigurationComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('showWizard', true);
    componentRef.setInput('igId', 'ig-001');
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize the form with default values', () => {
    expect(component.autoUploadForm).toBeDefined();
    expect(component.autoUploadForm.controls['ftpHost'].value).toBe('');
    expect(component.autoUploadForm.controls['ftpPort'].value).toBe('');
    expect(component.autoUploadForm.controls['uploadDay'].value).toBe('-1');
    expect(component.autoUploadForm.controls['uploadHour'].value).toBe('-1');
    expect(component.autoUploadForm.controls['autoExtractZip'].value).toBe(
      false
    );
  });

  it('should build library section tree on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockInterestGroupService.getInterestGroupAsync).toHaveBeenCalledWith(
      {
        id: 'ig-001',
      }
    );
    expect(mockNodesService.getPathAsync).toHaveBeenCalledWith({
      id: 'lib-123',
    });
    expect(component.libraryRoot()).toBeDefined();
    expect(component.libraryRoot()?.nodeId).toBe('lib-123');
  });

  describe('testConnection', () => {
    it('should set connectionResult on success', async () => {
      component.autoUploadForm.controls['ftpHost'].setValue('host');
      component.autoUploadForm.controls['ftpPort'].setValue('21');
      component.autoUploadForm.controls['username'].setValue('user');
      component.autoUploadForm.controls['password'].setValue('pass');
      component.autoUploadForm.controls['pathToFile'].setValue('/path');

      await component.testConnection();

      expect(component.connectionResult()).toBe(1);
      expect(component.testedOnce()).toBe(true);
    });

    it('should set connectionResult to -1 on error', async () => {
      mockFtpService.testFTPConnectionOnServerAsync.mockRejectedValueOnce(
        new Error('fail')
      );

      await component.testConnection();

      expect(component.connectionResult()).toBe(-1);
    });
  });

  describe('setDestination', () => {
    it('should set destination value from tree node', () => {
      const node = new TreeNode('Folder', 'node-123');
      component.setDestination(node);
      expect(component.autoUploadForm.controls['destination'].value).toBe(
        'node-123'
      );
    });

    it('should clear destination if same node is selected again', () => {
      component.autoUploadForm.controls['destination'].setValue('node-123');
      const node = new TreeNode('Folder', 'node-123');
      component.setDestination(node);
      expect(
        component.autoUploadForm.controls['destination'].value
      ).toBeUndefined();
    });
  });

  describe('addConfiguration', () => {
    it('should call postAutoUploadEntry and emit configurationAdded', async () => {
      const emitSpy = vi.spyOn(component.configurationAdded, 'emit');
      component.autoUploadForm.patchValue({
        ftpHost: 'host',
        ftpPort: '21',
        username: 'user',
        password: 'pass',
        pathToFile: '/path',
        destination: 'dest-id',
        uploadDay: '2',
        uploadHour: '10',
        autoExtractZip: true,
        jobNotifications: true,
        emailRecipients: 'a@b.com\nc@d.com',
      });

      await component.addConfiguration();

      expect(
        mockAutoUploadService.postAutoUploadEntryAsync
      ).toHaveBeenCalledWith({
        id: 'ig-001',
        autoUploadConfiguration: expect.objectContaining({
          igName: 'ig-001',
          ftpHost: 'host',
          ftpPort: '21',
          ftpUsername: 'user',
          ftpPassword: 'pass',
          ftpPath: '/path',
          parentId: 'dest-id',
          dayChoice: '2',
          hourChoice: '10',
          autoExtract: true,
          jobNotifications: true,
          emails: 'a@b.com,c@d.com',
        }),
      });
      expect(emitSpy).toHaveBeenCalled();
      expect(component.processing()).toBe(false);
    });
  });

  describe('wizard navigation', () => {
    it('should advance wizard step when connection is successful', () => {
      component.connectionResult.set(1);
      component.nextWizardStep();
      expect(component.wizardStep()).toBe(2);
    });

    it('should not advance wizard step when connection failed', () => {
      component.connectionResult.set(0);
      component.nextWizardStep();
      expect(component.wizardStep()).toBe(1);
    });

    it('should go back one step', () => {
      component.wizardStep.set(3);
      component.previousWizardStep();
      expect(component.wizardStep()).toBe(2);
    });

    it('should report canGoBack correctly', () => {
      component.wizardStep.set(1);
      expect(component.canGoBack()).toBe(false);
      component.wizardStep.set(2);
      expect(component.canGoBack()).toBe(true);
      component.processing.set(true);
      expect(component.canGoBack()).toBe(false);
    });
  });

  describe('destinationSelected', () => {
    it('should return false when destination is empty', () => {
      component.autoUploadForm.controls['destination'].setValue('');
      expect(component.destinationSelected()).toBe(false);
    });

    it('should return true when destination has a value', () => {
      component.autoUploadForm.controls['destination'].setValue('node-id');
      expect(component.destinationSelected()).toBe(true);
    });
  });

  describe('cancelWizard', () => {
    it('should reset form and emit canceled', () => {
      const cancelSpy = vi.spyOn(component.canceled, 'emit');
      component.wizardStep.set(3);
      component.testedOnce.set(true);

      component.cancelWizard();

      expect(component.wizardStep()).toBe(1);
      expect(component.testedOnce()).toBe(false);
      expect(cancelSpy).toHaveBeenCalled();
    });
  });
});
