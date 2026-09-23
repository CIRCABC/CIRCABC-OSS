import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  KeywordDefinition,
  KeywordsService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { FileUploadItem } from 'app/group/library/upload-form/file-upload-item';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FileMetadataComponent } from './file-metadata.component';

function createMockFile(
  overrides: Partial<FileUploadItem> = {}
): FileUploadItem {
  return {
    id: 'file-1',
    file: new File([''], 'test.txt'),
    name: 'test.txt',
    ...overrides,
  };
}

describe('FileMetadataComponent', () => {
  const mockKeywordsService = {
    getKeywordDefinitionsAsync: vi.fn().mockResolvedValue([]),
  };

  const mockDynamicPropertiesService = {
    getDynamicPropertyDefinitionsAsync: vi.fn().mockResolvedValue([]),
  };

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({ uiLang: 'en' }),
  };

  const mockRoute = {
    params: of({ id: 'group-123' }),
  };

  async function createComponent(file?: FileUploadItem) {
    TestBed.configureTestingModule({
      imports: [FileMetadataComponent],
      providers: [
        provideNativeDateAdapter(),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: KeywordsService, useValue: mockKeywordsService },
        {
          provide: DynamicPropertiesService,
          useValue: mockDynamicPropertiesService,
        },
        { provide: LoginService, useValue: mockLoginService },
        { provide: ActivatedRoute, useValue: mockRoute },
      ],
    }).overrideComponent(FileMetadataComponent, {
      set: {
        imports: [TranslocoModule, I18nPipe],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    const fixture = TestBed.createComponent(FileMetadataComponent);
    if (file) {
      fixture.componentRef.setInput('file', file);
    }
    fixture.detectChanges();
    await fixture.whenStable();
    return fixture;
  }

  beforeEach(() => {
    vi.clearAllMocks();
    mockKeywordsService.getKeywordDefinitionsAsync.mockResolvedValue([]);
    mockDynamicPropertiesService.getDynamicPropertyDefinitionsAsync.mockResolvedValue(
      []
    );
  });

  it('should create', async () => {
    const fixture = await createComponent(createMockFile());
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should set groupNodeId from route params', async () => {
    const fixture = await createComponent(createMockFile());
    expect(fixture.componentInstance.groupNodeId()).toBe('group-123');
  });

  it('should initialize fileForm with default values', async () => {
    const file = createMockFile({ securityRanking: 'NORMAL', status: 'DRAFT' });
    const fixture = await createComponent(file);
    const comp = fixture.componentInstance;
    expect(comp.fileForm).toBeDefined();
    expect(comp.fileForm.controls.securityRanking.value).toBe('NORMAL');
    expect(comp.fileForm.controls.status.value).toBe('DRAFT');
  });

  it('should patch form with file data', async () => {
    const file = createMockFile({
      name: 'document.pdf',
      author: 'John',
      reference: 'REF-001',
    });
    const fixture = await createComponent(file);
    const comp = fixture.componentInstance;
    expect(comp.fileForm.controls.name.value).toBe('document.pdf');
    expect(comp.fileForm.controls.author.value).toBe('John');
    expect(comp.fileForm.controls.reference.value).toBe('REF-001');
  });

  it('should call getDynamicPropertyDefinitions with groupNodeId', async () => {
    await createComponent(createMockFile());
    expect(
      mockDynamicPropertiesService.getDynamicPropertyDefinitionsAsync
    ).toHaveBeenCalledWith({ id: 'group-123' });
  });

  it('should call getKeywordDefinitions with groupNodeId', async () => {
    await createComponent(createMockFile());
    expect(mockKeywordsService.getKeywordDefinitionsAsync).toHaveBeenCalledWith(
      {
        id: 'group-123',
      }
    );
  });

  it('should add dynamic property controls to the form', async () => {
    const dynProps: DynamicPropertyDefinition[] = [
      { index: 1, title: { en: 'Prop 1' }, propertyType: 'TEXT_FIELD' },
      { index: 2, title: { en: 'Prop 2' }, propertyType: 'DATE_FIELD' },
    ];
    mockDynamicPropertiesService.getDynamicPropertyDefinitionsAsync.mockResolvedValue(
      dynProps
    );

    const fixture = await createComponent(createMockFile());
    const comp = fixture.componentInstance;
    expect(comp.fileForm.controls['dynAttr1']).toBeDefined();
    expect(comp.fileForm.controls['dynAttr2']).toBeDefined();
  });

  describe('property type checks', () => {
    const textField: DynamicPropertyDefinition = {
      title: { en: 'T' },
      propertyType: 'TEXT_FIELD',
    };
    const dateField: DynamicPropertyDefinition = {
      title: { en: 'D' },
      propertyType: 'DATE_FIELD',
    };
    const textArea: DynamicPropertyDefinition = {
      title: { en: 'A' },
      propertyType: 'TEXT_AREA',
    };
    const selection: DynamicPropertyDefinition = {
      title: { en: 'S' },
      propertyType: 'SELECTION',
    };
    const multiSelection: DynamicPropertyDefinition = {
      title: { en: 'M' },
      propertyType: 'MULTI_SELECTION',
    };

    it('should identify date fields', async () => {
      const fixture = await createComponent(createMockFile());
      const comp = fixture.componentInstance;
      expect(comp.isDateField(dateField)).toBe(true);
      expect(comp.isDateField(textField)).toBe(false);
    });

    it('should identify text fields', async () => {
      const fixture = await createComponent(createMockFile());
      const comp = fixture.componentInstance;
      expect(comp.isTextField(textField)).toBe(true);
      expect(comp.isTextField(dateField)).toBe(false);
    });

    it('should identify text areas', async () => {
      const fixture = await createComponent(createMockFile());
      const comp = fixture.componentInstance;
      expect(comp.isTextArea(textArea)).toBe(true);
      expect(comp.isTextArea(textField)).toBe(false);
    });

    it('should identify selection or multi-selection', async () => {
      const fixture = await createComponent(createMockFile());
      const comp = fixture.componentInstance;
      expect(comp.isSelectionOrMultiSelection(selection)).toBe(true);
      expect(comp.isSelectionOrMultiSelection(multiSelection)).toBe(true);
      expect(comp.isSelectionOrMultiSelection(textField)).toBe(false);
    });

    it('should identify multi-selection', async () => {
      const fixture = await createComponent(createMockFile());
      const comp = fixture.componentInstance;
      expect(comp.isMultiSelection(multiSelection)).toBe(true);
      expect(comp.isMultiSelection(selection)).toBe(false);
    });
  });

  describe('toggleKeyword', () => {
    it('should add keyword when not already selected', async () => {
      const file = createMockFile({ keywords: [] });
      const keyword: KeywordDefinition = {
        id: 'kw-1',
        title: { en: 'Keyword 1' },
      };
      mockKeywordsService.getKeywordDefinitionsAsync.mockResolvedValue([
        keyword,
      ]);
      const fixture = await createComponent(file);
      const comp = fixture.componentInstance;

      comp.toggleKeyword(keyword);

      expect(comp.selectedKeywords).toContain(keyword);
    });

    it('should remove keyword when already selected', async () => {
      const keyword: KeywordDefinition = {
        id: 'kw-1',
        title: { en: 'Keyword 1' },
      };
      mockKeywordsService.getKeywordDefinitionsAsync.mockResolvedValue([
        keyword,
      ]);
      const file = createMockFile({ keywords: ['kw-1'] });
      const fixture = await createComponent(file);
      const comp = fixture.componentInstance;

      comp.selectedKeywords = [keyword];
      // Ensure file.keywords reflects the expected state before toggling
      file.keywords = ['kw-1'];
      comp.toggleKeyword(keyword);

      expect(comp.selectedKeywords).not.toContain(keyword);
    });
  });

  describe('isSelectedKeyword', () => {
    it('should return true when keyword is in file keywords', async () => {
      const file = createMockFile({ keywords: ['kw-1'] });
      const fixture = await createComponent(file);
      const comp = fixture.componentInstance;

      const keyword: KeywordDefinition = {
        id: 'kw-1',
        title: { en: 'Keyword 1' },
      };
      // Ensure file.keywords reflects the expected state after component init
      file.keywords = ['kw-1'];
      expect(comp.isSelectedKeyword(keyword)).toBe(true);
    });

    it('should return false when keyword is not in file keywords', async () => {
      const file = createMockFile({ keywords: ['kw-2'] });
      const fixture = await createComponent(file);
      const comp = fixture.componentInstance;

      const keyword: KeywordDefinition = {
        id: 'kw-1',
        title: { en: 'Keyword 1' },
      };
      expect(comp.isSelectedKeyword(keyword)).toBe(false);
    });
  });

  describe('filteredKeywords', () => {
    it('should filter keywords by search value', async () => {
      mockKeywordsService.getKeywordDefinitionsAsync.mockResolvedValue([
        { id: '1', title: { en: 'Angular' } },
        { id: '2', title: { en: 'React' } },
      ]);
      const fixture = await createComponent(createMockFile());
      const comp = fixture.componentInstance;

      comp.filterForm.controls['keywordSearch'].setValue('ang');

      expect(comp.filteredKeywords).toHaveLength(1);
      expect(comp.filteredKeywords[0].title['en']).toBe('Angular');
    });

    it('should return all keywords when search is empty', async () => {
      mockKeywordsService.getKeywordDefinitionsAsync.mockResolvedValue([
        { id: '1', title: { en: 'Angular' } },
        { id: '2', title: { en: 'React' } },
      ]);
      const fixture = await createComponent(createMockFile());
      const comp = fixture.componentInstance;

      comp.filterForm.controls['keywordSearch'].setValue('');

      expect(comp.filteredKeywords).toHaveLength(2);
    });
  });

  describe('makeAsPivot / cancelPivot', () => {
    it('should set file as pivot with selected language', async () => {
      const file = createMockFile();
      const fixture = await createComponent(file);
      const comp = fixture.componentInstance;

      comp.pivotForm.controls['pivotLang'].setValue('fr');
      comp.makeAsPivot();

      expect(file.isPivot).toBe(true);
      expect(file.lang).toBe('fr');
    });

    it('should cancel pivot status', async () => {
      const file = createMockFile({ isPivot: true, lang: 'fr' });
      const fixture = await createComponent(file);
      const comp = fixture.componentInstance;

      comp.cancelPivot();

      expect(file.isPivot).toBe(false);
      expect(file.lang).toBe('');
    });
  });

  describe('defineAsTranslation / cancelTranslation', () => {
    it('should set file as translation', async () => {
      const file = createMockFile();
      const fixture = await createComponent(file);
      const comp = fixture.componentInstance;

      comp.translationForm.controls['pivotId'].setValue('pivot-1', {
        emitEvent: false,
      });
      comp.translationForm.controls['translationLang'].setValue('de');
      comp.defineAsTranslation();

      expect(file.isTranslation).toBe(true);
      expect(file.translationOf).toBe('pivot-1');
      expect(file.lang).toBe('de');
    });

    it('should cancel translation status', async () => {
      const file = createMockFile({
        isTranslation: true,
        translationOf: 'pivot-1',
        lang: 'de',
      });
      const fixture = await createComponent(file);
      const comp = fixture.componentInstance;

      comp.cancelTranslation();

      expect(file.isTranslation).toBe(false);
      expect(file.translationOf).toBeUndefined();
      expect(file.lang).toBeUndefined();
    });
  });

  describe('getDisabledLang', () => {
    it('should populate disabledLangs from pivots and translations', async () => {
      const fixture = await createComponent(createMockFile());
      const comp = fixture.componentInstance;

      fixture.componentRef.setInput('pivots', [
        createMockFile({ id: 'pivot-1', lang: 'en', isPivot: true }),
      ]);
      fixture.componentRef.setInput('translations', [
        createMockFile({
          id: 'trans-1',
          lang: 'fr',
          isTranslation: true,
          translationOf: 'pivot-1',
        }),
      ]);
      fixture.detectChanges();

      comp.getDisabledLang('pivot-1');

      expect(comp.disabledLangs()).toContain('en');
      expect(comp.disabledLangs()).toContain('fr');
    });

    it('should clear disabledLangs when pivotId is undefined', async () => {
      const fixture = await createComponent(createMockFile());
      const comp = fixture.componentInstance;

      comp.disabledLangs.set(['en', 'fr']);
      comp.getDisabledLang(undefined);

      expect(comp.disabledLangs()).toEqual([]);
    });
  });

  describe('compareFn', () => {
    it('should return true when both options are equal', async () => {
      const fixture = await createComponent(createMockFile());
      expect(fixture.componentInstance.compareFn('a', 'a')).toBe(true);
    });

    it('should return false when options differ', async () => {
      const fixture = await createComponent(createMockFile());
      expect(fixture.componentInstance.compareFn('a', 'b')).toBe(false);
    });

    it('should return false when an option is undefined', async () => {
      const fixture = await createComponent(createMockFile());
      expect(fixture.componentInstance.compareFn(undefined, 'b')).toBe(false);
    });
  });

  describe('lang getter', () => {
    it('should return uiLang from login service', async () => {
      const fixture = await createComponent(createMockFile());
      expect(fixture.componentInstance.lang).toBe('en');
    });
  });

  describe('updateFileProperties', () => {
    it('should emit fileChange with updated properties', async () => {
      const file = createMockFile();
      const fixture = await createComponent(file);
      const comp = fixture.componentInstance;

      const emitSpy = vi.spyOn(comp.fileChange, 'emit');
      comp.fileForm.controls['name'].setValue('updated.txt');
      comp.fileForm.controls['author'].setValue('Author');
      comp.updateFileProperties();

      expect(emitSpy).toHaveBeenCalled();
      expect(file.name).toBe('updated.txt');
      expect(file.author).toBe('Author');
    });

    it('should not emit when file is undefined', async () => {
      const fixture = await createComponent();
      const comp = fixture.componentInstance;

      const emitSpy = vi.spyOn(comp.fileChange, 'emit');
      comp.updateFileProperties();

      expect(emitSpy).not.toHaveBeenCalled();
    });
  });
});
