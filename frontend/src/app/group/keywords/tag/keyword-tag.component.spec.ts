import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { KeywordDefinition, KeywordsService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { KeywordTagComponent } from './keyword-tag.component';

describe('KeywordTagComponent', () => {
  let component: KeywordTagComponent;
  let componentRef: ComponentRef<KeywordTagComponent>;
  let fixture: ComponentFixture<KeywordTagComponent>;

  const mockKeywordsService = {
    deleteKeyword: vi.fn().mockReturnValue(of(undefined)),
    deleteKeywordAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockTranslocoService = {
    getActiveLang: vi.fn().mockReturnValue('en'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KeywordTagComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: KeywordsService, useValue: mockKeywordsService },
        { provide: TranslocoService, useValue: mockTranslocoService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(KeywordTagComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  const keyword: KeywordDefinition = {
    id: 'kw1',
    title: { en: 'Test', fr: 'Essai' },
  };

  it('should create', () => {
    componentRef.setInput('keyword', keyword);
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('getTitleKeys', () => {
    it('should return the specified lang if keyword title contains it', () => {
      componentRef.setInput('keyword', keyword);
      componentRef.setInput('lang', 'fr');
      fixture.detectChanges();

      expect(component.getTitleKeys(keyword)).toEqual(['fr']);
    });

    it('should fall back to active lang if specified lang not in title', () => {
      componentRef.setInput('keyword', keyword);
      componentRef.setInput('lang', 'de');
      fixture.detectChanges();

      mockTranslocoService.getActiveLang.mockReturnValue('en');
      expect(component.getTitleKeys(keyword)).toEqual(['en']);
    });

    it('should return first available key if neither specified nor active lang match', () => {
      const kwNoEn: KeywordDefinition = {
        id: 'kw2',
        title: { fr: 'Essai', bg: 'Тест' },
      };
      componentRef.setInput('keyword', kwNoEn);
      componentRef.setInput('lang', 'de');
      fixture.detectChanges();

      mockTranslocoService.getActiveLang.mockReturnValue('it');
      expect(component.getTitleKeys(kwNoEn)).toEqual(['fr']);
    });

    it('should return all title keys when lang input is not set', () => {
      componentRef.setInput('keyword', keyword);
      fixture.detectChanges();

      expect(component.getTitleKeys(keyword)).toEqual(['en', 'fr']);
    });
  });

  describe('removeKeyword', () => {
    it('should not call service when documentNode is undefined', async () => {
      componentRef.setInput('keyword', keyword);
      componentRef.setInput('removable', true);
      fixture.detectChanges();

      await component.removeKeyword(keyword);
      expect(mockKeywordsService.deleteKeyword).not.toHaveBeenCalled();
    });

    it('should not call service when removable is false', async () => {
      componentRef.setInput('keyword', keyword);
      componentRef.setInput('documentNode', { id: 'node1' });
      componentRef.setInput('removable', false);
      fixture.detectChanges();

      await component.removeKeyword(keyword);
      expect(mockKeywordsService.deleteKeyword).not.toHaveBeenCalled();
    });

    it('should call deleteKeyword and emit removed', async () => {
      componentRef.setInput('keyword', keyword);
      componentRef.setInput('documentNode', { id: 'node1' });
      componentRef.setInput('removable', true);
      fixture.detectChanges();

      const removedSpy = vi.fn();
      component.removed.subscribe(removedSpy);

      await component.removeKeyword(keyword);

      expect(mockKeywordsService.deleteKeywordAsync).toHaveBeenCalledWith({
        id: 'node1',
        keywordId: 'kw1',
      });
      expect(removedSpy).toHaveBeenCalled();
      expect(component.deleting()).toBe(false);
    });
  });

  describe('getLanguageName', () => {
    it('should return language name for a known code', () => {
      componentRef.setInput('keyword', keyword);
      fixture.detectChanges();

      expect(component.getLanguageName('bg')).toBe('български');
    });

    it('should return empty string for unknown code', () => {
      componentRef.setInput('keyword', keyword);
      fixture.detectChanges();

      expect(component.getLanguageName('xx')).toBe('');
    });
  });
});
