import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AlfrescoService } from 'app/core/alfresco.service';
import { LoginService } from 'app/core/login.service';
import { type SelectableNode } from 'app/core/ui-model/index';
import type { ProgressBarEvent } from 'ngx-extended-pdf-viewer';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ContentPreviewExtendedComponent } from './content-preview-ext.component';

const mockLoginService = {
  isGuest: vi.fn().mockReturnValue(false),
  getTicket: vi.fn().mockReturnValue('test-ticket'),
};

const mockAlfrescoService = {
  getRendition: vi.fn().mockResolvedValue({ entry: { status: 'CREATED' } }),
};

function makeNode(mimetype?: string): SelectableNode {
  return {
    id: 'node-123',
    name: 'test-file.pdf',
    properties: mimetype ? { mimetype } : undefined,
  } as SelectableNode;
}

describe('ContentPreviewExtendedComponent', () => {
  let fixture: ComponentFixture<ContentPreviewExtendedComponent>;
  let component: ContentPreviewExtendedComponent;
  let componentRef: ComponentRef<ContentPreviewExtendedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContentPreviewExtendedComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: AlfrescoService, useValue: mockAlfrescoService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    document.exitFullscreen = vi.fn().mockResolvedValue(undefined);
    document.documentElement.requestFullscreen = vi
      .fn()
      .mockResolvedValue(undefined);

    fixture = TestBed.createComponent(ContentPreviewExtendedComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;

    componentRef.setInput('showModal', true);
    componentRef.setInput('documentId', 'doc-1');
    componentRef.setInput('contentURL', 'http://example.com/content');
    componentRef.setInput('content', makeNode('application/pdf'));
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  describe('ngOnChanges', () => {
    it('should set previewReady to true for PDF content', async () => {
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.previewReady()).toBe(true);
    });

    it('should set isImagePreview to true for image content', async () => {
      componentRef.setInput('content', makeNode('image/png'));
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.isImagePreview()).toBe(true);
      expect(component.isDocumentPreview()).toBe(false);
    });

    it('should set isVideoPreview to true for video content', async () => {
      componentRef.setInput('content', makeNode('video/mp4'));
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.isVideoPreview()).toBe(true);
      expect(component.isDocumentPreview()).toBe(false);
    });

    it('should set isAudioPreview to true for audio content', async () => {
      componentRef.setInput('content', makeNode('audio/mpeg'));
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.isAudioPreview()).toBe(true);
      expect(component.isDocumentPreview()).toBe(false);
    });

    it('should reset error and progressing state', async () => {
      component.error.set(true);
      component.progressing.set(true);
      fixture.detectChanges();
      await component.ngOnChanges();
      expect(component.error()).toBe(false);
      expect(component.progressing()).toBe(false);
    });
  });

  describe('onError', () => {
    it('should set error state', () => {
      const err = new Error('test error');
      component.onError(err);
      expect(component.error()).toBe(true);
      expect(component.theError).toBe(err);
      expect(component.progressing()).toBe(false);
    });
  });

  describe('onProgress', () => {
    it('should update progress on load event', () => {
      const event: ProgressBarEvent = {
        type: 'load',
        percent: 50,
        source: null,
        total: 100,
      };
      component.onProgress(event);
      expect(component.progressing()).toBe(true);
      expect(component.progressPercent()).toBe(50);
      expect(component.progress).toBe(event);
    });

    it('should not update progress for print events', () => {
      const event: ProgressBarEvent = {
        type: 'print',
        percent: 30,
        source: null,
        total: 100,
      };
      component.onProgress(event);
      expect(component.progressing()).toBe(false);
    });
  });

  describe('mediaContentURL', () => {
    it('should return URL with ticket for authenticated user', () => {
      fixture.detectChanges();
      const url = component.mediaContentURL();
      expect(url).toContain('ticket=test-ticket');
      expect(url).toContain('node-123');
    });

    it('should return guest URL when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      fixture.detectChanges();
      const url = component.mediaContentURL();
      expect(url).toContain('guest=true');
      expect(url).not.toContain('ticket=');
    });
  });

  describe('close', () => {
    it('should reset error and previewReady and emit contentPreviewed', () => {
      component.error.set(true);
      component.previewReady.set(true);
      vi.spyOn(component.contentPreviewed, 'emit');
      component.close();
      expect(component.error()).toBe(false);
      expect(component.previewReady()).toBe(false);
      expect(component.contentPreviewed.emit).toHaveBeenCalled();
    });
  });
});
