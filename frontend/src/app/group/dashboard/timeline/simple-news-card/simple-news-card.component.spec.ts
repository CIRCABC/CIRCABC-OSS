import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { News } from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { SimpleNewsCardComponent } from './simple-news-card.component';

describe('SimpleNewsCardComponent', () => {
  let component: SimpleNewsCardComponent;
  let componentRef: ComponentRef<SimpleNewsCardComponent>;
  let fixture: ComponentFixture<SimpleNewsCardComponent>;
  let mockSaveAsService: { saveAs: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockSaveAsService = { saveAs: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [SimpleNewsCardComponent],
      providers: [
        { provide: SaveAsService, useValue: mockSaveAsService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(SimpleNewsCardComponent, {
        set: { template: '', schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(SimpleNewsCardComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  describe('titleIsNotEmpty', () => {
    it('should return true when title is defined', () => {
      componentRef.setInput('news', { title: { en: 'Test' } } as News);
      fixture.detectChanges();
      expect(component.titleIsNotEmpty()).toBe(true);
    });

    it('should return false when news is undefined', () => {
      fixture.detectChanges();
      expect(component.titleIsNotEmpty()).toBe(false);
    });
  });

  describe('isImportant', () => {
    it('should return true when newsLayout is important', () => {
      componentRef.setInput('news', {
        properties: { newsLayout: 'important' },
      } as News);
      fixture.detectChanges();
      expect(component.isImportant()).toBe(true);
    });

    it('should return false when newsLayout is not important', () => {
      componentRef.setInput('news', {
        properties: { newsLayout: 'normal' },
      } as News);
      fixture.detectChanges();
      expect(component.isImportant()).toBe(false);
    });

    it('should return false when no properties', () => {
      componentRef.setInput('news', {} as News);
      fixture.detectChanges();
      expect(component.isImportant()).toBe(false);
    });
  });

  describe('isReminder', () => {
    it('should return true when layout is reminder', () => {
      componentRef.setInput('news', {
        properties: { newsLayout: 'reminder', layout: 'reminder' },
      } as News);
      fixture.detectChanges();
      expect(component.isReminder()).toBe(true);
    });

    it('should return false when no properties', () => {
      componentRef.setInput('news', {} as News);
      fixture.detectChanges();
      expect(component.isReminder()).toBe(false);
    });
  });

  describe('pattern checks', () => {
    it('isImage should return true for image pattern', () => {
      componentRef.setInput('news', {
        properties: { newsPattern: 'image' },
      } as News);
      fixture.detectChanges();
      expect(component.isImage()).toBe(true);
      expect(component.isDocument()).toBe(false);
      expect(component.isDate()).toBe(false);
    });

    it('isDocument should return true for document pattern', () => {
      componentRef.setInput('news', {
        properties: { newsPattern: 'document' },
      } as News);
      fixture.detectChanges();
      expect(component.isDocument()).toBe(true);
    });

    it('isDate should return true for date pattern', () => {
      componentRef.setInput('news', {
        properties: { newsPattern: 'date' },
      } as News);
      fixture.detectChanges();
      expect(component.isDate()).toBe(true);
    });

    it('isIframe should return true for iframe pattern', () => {
      componentRef.setInput('news', {
        properties: { newsPattern: 'iframe' },
      } as News);
      fixture.detectChanges();
      expect(component.isIframe()).toBe(true);
    });

    it('isIframe should return false when no newsPattern', () => {
      componentRef.setInput('news', {} as News);
      fixture.detectChanges();
      expect(component.isIframe()).toBe(false);
    });
  });

  describe('saveFile', () => {
    it('should call saveAsService when id and name are defined', () => {
      componentRef.setInput('news', {
        properties: { newsDocId: '123', newsDocName: 'file.pdf' },
      } as News);
      fixture.detectChanges();
      component.saveFile();
      expect(mockSaveAsService.saveAs).toHaveBeenCalledWith('123', 'file.pdf');
    });

    it('should not call saveAsService when id is undefined', () => {
      componentRef.setInput('news', {
        properties: { newsDocName: 'file.pdf' },
      } as News);
      fixture.detectChanges();
      component.saveFile();
      expect(mockSaveAsService.saveAs).not.toHaveBeenCalled();
    });
  });

  describe('prepareUrl', () => {
    it('should return sanitized style when url has changingThisBreaksApplicationSecurity', () => {
      const result = component.prepareUrl({
        changingThisBreaksApplicationSecurity: 'http://example.com/img.png',
      });
      expect(result).toBeDefined();
    });

    it('should return undefined when url does not have the property', () => {
      expect(component.prepareUrl(null)).toBeUndefined();
      expect(component.prepareUrl({})).toBeUndefined();
    });
  });

  describe('getSafeUrl', () => {
    it('should return news url when defined', () => {
      componentRef.setInput('news', { url: 'http://example.com' } as News);
      fixture.detectChanges();
      expect(component.getSafeUrl()).toBe('http://example.com');
    });

    it('should return empty string when no url', () => {
      componentRef.setInput('news', {} as News);
      fixture.detectChanges();
      expect(component.getSafeUrl()).toBe('');
    });
  });

  describe('getSanitizedContent', () => {
    it('should return sanitized HTML when newsContent exists', () => {
      componentRef.setInput('news', {
        properties: { newsContent: '<p>Hello</p>' },
      } as News);
      fixture.detectChanges();
      const result = component.getSanitizedContent();
      expect(result).toBeDefined();
      expect(result).not.toBe('');
    });

    it('should return empty string when no newsContent', () => {
      componentRef.setInput('news', {} as News);
      fixture.detectChanges();
      expect(component.getSanitizedContent()).toBe('');
    });
  });

  describe('getNewsSize', () => {
    it('should return size as number', () => {
      componentRef.setInput('news', { size: 3 } as News);
      fixture.detectChanges();
      expect(component.getNewsSize()).toBe(3);
    });

    it('should return 1 when size is undefined', () => {
      componentRef.setInput('news', {} as News);
      fixture.detectChanges();
      expect(component.getNewsSize()).toBe(1);
    });
  });

  describe('hasValidUrl', () => {
    it('should return true for a valid URL', () => {
      componentRef.setInput('news', {
        properties: { newsUrl: 'http://example.com' },
      } as News);
      fixture.detectChanges();
      expect(component.hasValidUrl()).toBe(true);
    });

    it('should return false for empty newsUrl', () => {
      componentRef.setInput('news', {
        properties: { newsUrl: '' },
      } as News);
      fixture.detectChanges();
      expect(component.hasValidUrl()).toBe(false);
    });

    it('should return false when no properties', () => {
      componentRef.setInput('news', {} as News);
      fixture.detectChanges();
      expect(component.hasValidUrl()).toBe(false);
    });
  });

  describe('getAuthor', () => {
    it('should return modifier when defined', () => {
      componentRef.setInput('news', { modifier: 'Jane' } as News);
      fixture.detectChanges();
      expect(component.getAuthor()).toBe('Jane');
    });

    it('should return John Doe when modifier is undefined', () => {
      componentRef.setInput('news', {} as News);
      fixture.detectChanges();
      expect(component.getAuthor()).toBe('John Doe');
    });
  });

  describe('getDate', () => {
    it('should return date from modified field', () => {
      componentRef.setInput('news', {
        modified: '2024-01-15T10:00:00Z',
      } as News);
      fixture.detectChanges();
      expect(component.getDate()).toEqual(new Date('2024-01-15T10:00:00Z'));
    });

    it('should return current date when modified is undefined', () => {
      componentRef.setInput('news', {} as News);
      fixture.detectChanges();
      const result = component.getDate();
      expect(result).toBeInstanceOf(Date);
    });
  });
});
