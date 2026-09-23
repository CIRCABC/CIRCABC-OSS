import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { HelpArticle } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ArticleListSelectComponent } from './article-list-select.component';

describe('ArticleListSelectComponent', () => {
  let component: ArticleListSelectComponent;
  let componentRef: ComponentRef<ArticleListSelectComponent>;
  let fixture: ComponentFixture<ArticleListSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ArticleListSelectComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArticleListSelectComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default articles to empty array', () => {
    expect(component.articles()).toEqual([]);
  });

  it('should default currentId to undefined', () => {
    expect(component.currentId()).toBeUndefined();
  });

  it('should render articles list', () => {
    const articles: HelpArticle[] = [
      { id: '1', title: { en: 'First' } },
      { id: '2', title: { en: 'Second' } },
    ];
    componentRef.setInput('articles', articles);
    fixture.detectChanges();

    const items = fixture.nativeElement.querySelectorAll('li');
    expect(items).toHaveLength(2);
  });

  it('should apply selected class to current article', () => {
    const articles: HelpArticle[] = [
      { id: '1', title: { en: 'First' } },
      { id: '2', title: { en: 'Second' } },
    ];
    componentRef.setInput('articles', articles);
    componentRef.setInput('currentId', '2');
    fixture.detectChanges();

    const items = fixture.nativeElement.querySelectorAll('li');
    expect(items[0].classList.contains('selected')).toBe(false);
    expect(items[1].classList.contains('selected')).toBe(true);
  });
});
