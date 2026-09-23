import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { HelpArticle, HelpService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FaqHighlightsComponent } from './faq-highlights.component';

const mockArticles: HelpArticle[] = [
  { id: '1', title: { en: 'Article 1' }, highlighted: true },
  { id: '2', title: { en: 'Article 2' }, highlighted: true },
  { id: '3', title: { en: 'Article 3' }, highlighted: true },
  { id: '4', title: { en: 'Article 4' }, highlighted: true },
];

const mockHelpService = {
  getHelpHighlightedArticlesAsync: vi.fn(),
};

describe('FaqHighlightsComponent', () => {
  let component: FaqHighlightsComponent;
  let fixture: ComponentFixture<FaqHighlightsComponent>;

  beforeEach(async () => {
    mockHelpService.getHelpHighlightedArticlesAsync.mockResolvedValue(
      mockArticles
    );

    await TestBed.configureTestingModule({
      imports: [FaqHighlightsComponent],
      providers: [
        provideRouter([]),
        { provide: HelpService, useValue: mockHelpService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FaqHighlightsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load highlighted articles and display first 3 on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.highlightedArticles()).toEqual(mockArticles);
    expect(component.displayedArticles()).toEqual(mockArticles.slice(0, 3));
  });

  it('should display all articles if fewer than 3', async () => {
    const twoArticles: HelpArticle[] = [{ id: '1' }, { id: '2' }];
    mockHelpService.getHelpHighlightedArticlesAsync.mockResolvedValue(
      twoArticles
    );

    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.displayedArticles()).toEqual(twoArticles);
  });

  it('should handle error on init', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    mockHelpService.getHelpHighlightedArticlesAsync.mockRejectedValue(
      new Error('fail')
    );

    fixture.detectChanges();
    await fixture.whenStable();

    expect(consoleSpy).toHaveBeenCalled();
    expect(component.highlightedArticles()).toEqual([]);
    consoleSpy.mockRestore();
  });

  it('should rotate articles backward on previous()', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.previous();

    expect(component.highlightedArticles()[0].id).toBe('4');
    expect(component.displayedArticles()).toHaveLength(3);
  });

  it('should rotate articles forward on next()', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.next();

    expect(component.highlightedArticles()[0].id).toBe('2');
    expect(component.displayedArticles()).toHaveLength(3);
  });

  it('should do nothing on previous() with empty array', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.highlightedArticles.set([]);

    component.previous();

    expect(component.displayedArticles()).toEqual([]);
  });

  it('should do nothing on next() with empty array', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.highlightedArticles.set([]);

    component.next();

    expect(component.displayedArticles()).toEqual([]);
  });
});
