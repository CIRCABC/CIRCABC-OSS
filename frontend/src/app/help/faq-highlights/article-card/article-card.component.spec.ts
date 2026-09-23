import { DatePipe } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { type HelpArticle } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ArticleCardComponent } from './article-card.component';

describe('ArticleCardComponent', () => {
  const mockArticle: HelpArticle = {
    id: 'article-1',
    parentId: 'category-1',
    title: { en: 'Test Article' },
    content: { en: '<p>Test content</p>' },
    lastUpdate: '2026-01-15',
    highlighted: true,
  };

  function createComponent(article: HelpArticle = mockArticle) {
    TestBed.configureTestingModule({
      imports: [ArticleCardComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(ArticleCardComponent, {
      set: {
        imports: [TranslocoModule, I18nPipe, DatePipe],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    const fixture = TestBed.createComponent(ArticleCardComponent);
    fixture.componentRef.setInput('article', article);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should expose the article input', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance.article()).toEqual(mockArticle);
  });
});
