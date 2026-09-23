import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  ContentService,
  InformationService,
  News,
  NodesService,
} from 'app/core/generated/circabc';
import { UploadService } from 'app/core/upload.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddNewsComponent } from './add-news.component';

const mockRouter = {
  navigate: vi.fn().mockResolvedValue(true),
};

const mockInformationService = {
  getNewsAsync: vi.fn(),
  postInformationNews: vi.fn(),
  putNews: vi.fn(),
  postInformationNewsAsync: vi.fn().mockResolvedValue({ id: 'created-1' }),
  putNewsAsync: vi.fn().mockResolvedValue({ id: 'news-1' }),
};

const mockUploadService = {
  uploadNewFile: vi.fn().mockResolvedValue('node-id'),
};

const mockContentService = {
  deleteContentAsync: vi.fn().mockResolvedValue(undefined),
};

const mockNodesService = {
  getNodeAsync: vi.fn().mockResolvedValue({ id: 'info-node-id', name: 'Info' }),
};

async function setup(params: Record<string, string> = { id: 'group-1' }) {
  const mockRoute = { params: of(params) };

  TestBed.configureTestingModule({
    imports: [AddNewsComponent],
    providers: [
      provideNativeDateAdapter(),
      { provide: ActivatedRoute, useValue: mockRoute },
      { provide: Router, useValue: mockRouter },
      { provide: InformationService, useValue: mockInformationService },
      { provide: UploadService, useValue: mockUploadService },
      { provide: ContentService, useValue: mockContentService },
      { provide: NodesService, useValue: mockNodesService },
      provideTransloco({
        config: { defaultLang: 'en', availableLangs: ['en'] },
      }),
      {
        provide: TRANSLOCO_LOADER,
        useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
      },
    ],
  }).overrideComponent(AddNewsComponent, {
    set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
  });

  const fixture = TestBed.createComponent(AddNewsComponent);
  fixture.detectChanges();
  await fixture.whenStable();

  return { fixture, component: fixture.componentInstance, mockRoute };
}

describe('AddNewsComponent', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', async () => {
    const { component } = await setup();
    expect(component).toBeDefined();
  });

  describe('create mode', () => {
    it('should set groupId and initialize form', async () => {
      const { component } = await setup({ id: 'group-1' });

      expect(component.groupId).toBe('group-1');
      expect(component.newsForm).toBeDefined();
      expect(component.newsForm.value.pattern).toBe('text');
      expect(component.newsForm.value.layout).toBe('normal');
      expect(component.newsForm.value.size).toBe(1);
    });

    it('should not be in edit mode when newsId is undefined', async () => {
      const { component } = await setup({ id: 'group-1' });
      expect(component.inEditMode()).toBe(false);
    });
  });

  describe('edit mode', () => {
    const existingNews: News = {
      id: 'news-1',
      title: { en: 'Test Title' },
      content: '<p>Hello</p>',
      pattern: 'text',
      layout: 'normal',
      size: 2,
      date: '2025-01-15T00:00:00.000Z',
    };

    it('should be in edit mode and load news', async () => {
      mockInformationService.getNewsAsync.mockResolvedValue(existingNews);
      const { component } = await setup({ id: 'group-1', newsId: 'news-1' });

      expect(component.inEditMode()).toBe(true);
      expect(component.newsForm.value.content).toBe('<p>Hello</p>');
      expect(component.newsForm.value.size).toBe(2);
      expect(component.newsForm.value.layout).toBe('normal');
    });
  });

  describe('pattern selection helpers', () => {
    it('isDateSelected returns true when pattern is date', async () => {
      const { component } = await setup();
      component.newsForm.controls['pattern'].setValue('date');
      expect(component.isDateSelected()).toBe(true);
    });

    it('isFileOrImageSelected returns true for image pattern', async () => {
      const { component } = await setup();
      component.newsForm.controls['pattern'].setValue('image');
      expect(component.isFileOrImageSelected()).toBe(true);
    });

    it('isFileOrImageSelected returns true for document pattern', async () => {
      const { component } = await setup();
      component.newsForm.controls['pattern'].setValue('document');
      expect(component.isFileOrImageSelected()).toBe(true);
    });

    it('isIFrameSelected returns true for iframe pattern', async () => {
      const { component } = await setup();
      component.newsForm.controls['pattern'].setValue('iframe');
      expect(component.isIFrameSelected()).toBe(true);
    });
  });

  describe('isFormValid', () => {
    it('should return false when form is invalid', async () => {
      const { component } = await setup();
      expect(component.isFormValid()).toBe(false);
    });

    it('should return true when form is valid for text pattern', async () => {
      const { component } = await setup();
      component.newsForm.controls['title'].setValue({ en: 'Title' });
      component.newsForm.controls['content'].setValue('Some content');
      expect(component.isFormValid()).toBe(true);
    });

    it('should require file for document pattern in create mode', async () => {
      const { component } = await setup();
      component.newsForm.controls['title'].setValue({ en: 'Title' });
      component.newsForm.controls['content'].setValue('Some content');
      component.newsForm.controls['pattern'].setValue('document');
      expect(component.isFormValid()).toBe(false);

      component.filesToUpload = [new File(['data'], 'test.pdf')];
      expect(component.isFormValid()).toBe(true);
    });
  });

  describe('file handling', () => {
    it('getFileName returns empty string when no files', async () => {
      const { component } = await setup();
      expect(component.getFileName()).toBe('');
    });

    it('getFileName returns file name when file is set', async () => {
      const { component } = await setup();
      component.filesToUpload = [new File([''], 'report.pdf')];
      expect(component.getFileName()).toBe('report.pdf');
    });

    it('getFile returns undefined when no files', async () => {
      const { component } = await setup();
      expect(component.getFile()).toBeUndefined();
    });

    it('getFile returns the file when set', async () => {
      const { component } = await setup();
      const file = new File([''], 'report.pdf');
      component.filesToUpload = [file];
      expect(component.getFile()).toBe(file);
    });
  });

  describe('saveNews', () => {
    it('should call postInformationNews and navigate on success', async () => {
      mockInformationService.postInformationNewsAsync.mockResolvedValue({
        id: 'created-1',
        pattern: 'text',
      } as News);
      const { component } = await setup();
      component.newsForm.controls['title'].setValue({ en: 'New Title' });
      component.newsForm.controls['content'].setValue('Content');

      await component.saveNews();

      expect(
        mockInformationService.postInformationNewsAsync
      ).toHaveBeenCalledWith({
        id: 'group-1',
        news: expect.objectContaining({ content: 'Content' }),
      });
      expect(mockRouter.navigate).toHaveBeenCalled();
      expect(component.executing()).toBe(false);
    });

    it('should not navigate on failure', async () => {
      mockInformationService.postInformationNewsAsync.mockRejectedValueOnce(
        new Error('fail')
      );
      const { component } = await setup();
      component.newsForm.controls['title'].setValue({ en: 'New Title' });
      component.newsForm.controls['content'].setValue('Content');

      await component.saveNews();

      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(component.executing()).toBe(false);
    });
  });

  describe('updateNews', () => {
    const existingNews: News = {
      id: 'news-1',
      title: { en: 'Old Title' },
      content: '<p>Old</p>',
      pattern: 'text',
      layout: 'normal',
      size: 1,
    };

    it('should call putNews and navigate on success', async () => {
      mockInformationService.getNewsAsync.mockResolvedValue(existingNews);
      mockInformationService.putNewsAsync.mockResolvedValue({
        id: 'news-1',
        pattern: 'text',
      } as News);
      const { component } = await setup({ id: 'group-1', newsId: 'news-1' });
      component.newsForm.controls['content'].setValue('Updated');

      await component.updateNews();

      expect(mockInformationService.putNewsAsync).toHaveBeenCalledWith({
        id: 'news-1',
        news: expect.objectContaining({ content: 'Updated' }),
      });
      expect(mockRouter.navigate).toHaveBeenCalled();
      expect(component.executing()).toBe(false);
    });
  });

  describe('cancel', () => {
    it('should navigate back', async () => {
      const { component } = await setup();
      await component.cancel();
      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['../..'],
        expect.anything()
      );
    });
  });

  describe('getTypeOfCard', () => {
    it('should return pattern-layout-size string', async () => {
      const { component } = await setup();
      expect(component.getTypeOfCard()).toBe('text-normal-1');
    });
  });

  describe('validator changes on pattern switch to iframe', () => {
    it('should require url and remove title/content validators for iframe', async () => {
      const { component } = await setup();
      component.newsForm.controls['pattern'].setValue('iframe');

      expect(component.newsForm.controls['title'].valid).toBe(true);
      expect(component.newsForm.controls['content'].valid).toBe(true);
      expect(component.newsForm.controls['url'].valid).toBe(false);
    });

    it('should restore title/content validators when switching back from iframe', async () => {
      const { component } = await setup();
      component.newsForm.controls['pattern'].setValue('iframe');
      component.newsForm.controls['pattern'].setValue('text');

      // content is required and empty, so invalid
      expect(component.newsForm.controls['content'].valid).toBe(false);
      // title uses nonEmptyTitle which only returns error when dirty
      component.newsForm.controls['title'].markAsDirty();
      component.newsForm.controls['title'].updateValueAndValidity();
      expect(component.newsForm.controls['title'].valid).toBe(false);
      // url has no validators in text mode
      expect(component.newsForm.controls['url'].valid).toBe(true);
    });
  });
});
