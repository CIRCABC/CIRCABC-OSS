import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import { InformationService, News } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SERVER_URL } from 'app/core/variables';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { NewsCardComponent } from './news-card.component';

describe('NewsCardComponent', () => {
  let component: NewsCardComponent;
  let fixture: ComponentFixture<NewsCardComponent>;

  const mockInformationService = {
    deleteNews: vi.fn().mockReturnValue(of(undefined)),
    deleteNewsAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockSaveAsService = {
    saveAs: vi.fn(),
  };

  const mockLoginService = {
    getTicket: vi.fn().mockReturnValue('fake-ticket'),
    getCurrentUsername: vi.fn().mockReturnValue('user'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewsCardComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: InformationService, useValue: mockInformationService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: SERVER_URL, useValue: 'http://localhost/' },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(NewsCardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('titleIsNotEmpty', () => {
    it('should return false when news is undefined', () => {
      expect(component.titleIsNotEmpty()).toBe(false);
    });

    it('should return true when news has a title', () => {
      fixture.componentRef.setInput('news', { title: { en: 'Test' } } as News);
      expect(component.titleIsNotEmpty()).toBe(true);
    });
  });

  describe('isImage', () => {
    it('should return false when news has no pattern', () => {
      expect(component.isImage()).toBe(false);
    });

    it('should return true when pattern is image', () => {
      fixture.componentRef.setInput('news', { pattern: 'image' } as News);
      expect(component.isImage()).toBe(true);
    });

    it('should return false when pattern is not image', () => {
      fixture.componentRef.setInput('news', { pattern: 'text' } as News);
      expect(component.isImage()).toBe(false);
    });
  });

  describe('isLayout', () => {
    it('should return true for important layout', () => {
      fixture.componentRef.setInput('news', { layout: 'important' } as News);
      expect(component.isImportant()).toBe(true);
      expect(component.isReminder()).toBe(false);
    });

    it('should return true for reminder layout', () => {
      fixture.componentRef.setInput('news', { layout: 'reminder' } as News);
      expect(component.isReminder()).toBe(true);
      expect(component.isImportant()).toBe(false);
    });
  });

  describe('isDocument / isDate / isIframe', () => {
    it('should detect document pattern', () => {
      fixture.componentRef.setInput('news', { pattern: 'document' } as News);
      expect(component.isDocument()).toBe(true);
      expect(component.isDate()).toBe(false);
      expect(component.isIframe()).toBe(false);
    });

    it('should detect date pattern', () => {
      fixture.componentRef.setInput('news', { pattern: 'date' } as News);
      expect(component.isDate()).toBe(true);
    });

    it('should detect iframe pattern', () => {
      fixture.componentRef.setInput('news', { pattern: 'iframe' } as News);
      expect(component.isIframe()).toBe(true);
    });
  });

  describe('getNewsFileName / getNewsFileId / hasFile', () => {
    it('should return undefined when no files', () => {
      fixture.componentRef.setInput('news', {} as News);
      expect(component.getNewsFileName()).toBeUndefined();
      expect(component.getNewsFileId()).toBeUndefined();
      expect(component.hasFile()).toBe(false);
    });

    it('should return file info when files exist', () => {
      fixture.componentRef.setInput('news', {
        files: [{ id: '123', name: 'doc.pdf' }],
      } as News);
      expect(component.getNewsFileName()).toBe('doc.pdf');
      expect(component.getNewsFileId()).toBe('123');
      expect(component.hasFile()).toBe(true);
    });
  });

  describe('isNewsManage', () => {
    it('should return false when no permissions', () => {
      fixture.componentRef.setInput('news', {} as News);
      expect(component.isNewsManage()).toBe(false);
    });

    it('should return true when InfManage is ALLOWED', () => {
      fixture.componentRef.setInput('news', {
        permissions: { InfManage: 'ALLOWED' },
      } as News);
      expect(component.isNewsManage()).toBe(true);
    });

    it('should return true when InfAdmin is ALLOWED', () => {
      fixture.componentRef.setInput('news', {
        permissions: { InfAdmin: 'ALLOWED' },
      } as News);
      expect(component.isNewsManage()).toBe(true);
    });
  });

  describe('deleteNews', () => {
    it('should call informationService.deleteNews and emit SUCCEED', async () => {
      fixture.componentRef.setInput('news', { id: 'news-1' } as News);

      const emitSpy = vi.spyOn(component.newsDeleted, 'emit');
      await component.deleteNews();

      expect(mockInformationService.deleteNewsAsync).toHaveBeenCalledWith({
        id: 'news-1',
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.SUCCEED })
      );
    });

    it('should emit FAILED on error', async () => {
      mockInformationService.deleteNewsAsync.mockRejectedValueOnce(
        new Error('fail')
      );
      fixture.componentRef.setInput('news', { id: 'news-2' } as News);

      const emitSpy = vi.spyOn(component.newsDeleted, 'emit');
      await component.deleteNews();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.FAILED })
      );
    });

    it('should not call service when news has no id', async () => {
      fixture.componentRef.setInput('news', {} as News);
      mockInformationService.deleteNewsAsync.mockClear();

      await component.deleteNews();
      expect(mockInformationService.deleteNewsAsync).not.toHaveBeenCalled();
    });
  });

  describe('saveFile', () => {
    it('should call saveAsService.saveAs with file id and name', () => {
      fixture.componentRef.setInput('news', {
        files: [{ id: 'f1', name: 'report.pdf' }],
      } as News);

      component.saveFile();
      expect(mockSaveAsService.saveAs).toHaveBeenCalledWith('f1', 'report.pdf');
    });

    it('should not call saveAs when no files', () => {
      fixture.componentRef.setInput('news', {} as News);
      mockSaveAsService.saveAs.mockClear();

      component.saveFile();
      expect(mockSaveAsService.saveAs).not.toHaveBeenCalled();
    });
  });

  describe('getNewsSize', () => {
    it('should return size from news', () => {
      fixture.componentRef.setInput('news', { size: 3 } as News);
      expect(component.getNewsSize()).toBe(3);
    });

    it('should return 1 when no size', () => {
      fixture.componentRef.setInput('news', {} as News);
      expect(component.getNewsSize()).toBe(1);
    });
  });

  describe('hasValidUrl', () => {
    it('should return false when no url', () => {
      fixture.componentRef.setInput('news', {} as News);
      expect(component.hasValidUrl()).toBe(false);
    });

    it('should return true for a valid url', () => {
      fixture.componentRef.setInput('news', {
        url: 'https://example.com',
      } as News);
      expect(component.hasValidUrl()).toBe(true);
    });
  });

  describe('getAuthor', () => {
    it('should return modifier when not in preview', () => {
      fixture.componentRef.setInput('news', { modifier: 'admin' } as News);
      fixture.componentRef.setInput('preview', false);
      expect(component.getAuthor()).toBe('admin');
    });

    it('should return John Doe in preview mode', () => {
      fixture.componentRef.setInput('news', { modifier: 'admin' } as News);
      fixture.componentRef.setInput('preview', true);
      expect(component.getAuthor()).toBe('John Doe');
    });
  });

  describe('isEdited', () => {
    it('should return true when modified differs from created', () => {
      fixture.componentRef.setInput('news', {
        modified: '2024-01-15T10:30:00',
        created: '2024-01-10T08:00:00',
      } as News);
      expect(component.isEdited()).toBe(true);
    });

    it('should return false when modified equals created', () => {
      fixture.componentRef.setInput('news', {
        modified: '2024-01-10T08:00:00',
        created: '2024-01-10T08:00:00',
      } as News);
      expect(component.isEdited()).toBe(false);
    });

    it('should return false when no dates', () => {
      fixture.componentRef.setInput('news', {} as News);
      expect(component.isEdited()).toBe(false);
    });
  });

  describe('propagateClick', () => {
    it('should emit newsClicked', () => {
      const emitSpy = vi.spyOn(component.newsClicked, 'emit');
      component.propagateClick();
      expect(emitSpy).toHaveBeenCalled();
    });
  });

  describe('highlightedMaxWindowAction', () => {
    it('should set highlightedMaximized and emit', () => {
      const emitSpy = vi.spyOn(component.highlightedMaxWindow, 'emit');
      component.highlightedMaxWindowAction(true);
      expect(component.highlightedMaximized).toBe(true);
      expect(emitSpy).toHaveBeenCalledWith(true);
    });
  });

  describe('isPDFDocument', () => {
    it('should return true when file name contains .pdf', () => {
      fixture.componentRef.setInput('news', {
        files: [{ id: '1', name: 'report.pdf' }],
      } as News);
      expect(component.isPDFDocument()).toBe(true);
    });

    it('should return false when file name does not contain .pdf', () => {
      fixture.componentRef.setInput('news', {
        files: [{ id: '1', name: 'image.png' }],
      } as News);
      expect(component.isPDFDocument()).toBe(false);
    });
  });

  describe('getSanitizedContent', () => {
    it('should return sanitized HTML when content exists', () => {
      fixture.componentRef.setInput('news', {
        content: '<p>Hello</p>',
      } as News);
      const result = component.getSanitizedContent();
      expect(result).toBeTruthy();
    });

    it('should return empty string when no content', () => {
      fixture.componentRef.setInput('news', {} as News);
      expect(component.getSanitizedContent()).toBe('');
    });
  });
});
