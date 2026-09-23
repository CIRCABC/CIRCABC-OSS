import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { HelpCategory } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CategoryListSelectComponent } from './category-list-select.component';

@Pipe({ name: 'cbcI18n' })
class MockI18nPipe implements PipeTransform {
  transform(value: { [key: string]: string } | undefined): string {
    return value?.['en'] ?? '';
  }
}

describe('CategoryListSelectComponent', () => {
  function createComponent(
    inputs: { categories?: HelpCategory[]; currentId?: string } = {}
  ) {
    TestBed.configureTestingModule({
      imports: [CategoryListSelectComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(CategoryListSelectComponent, {
      set: {
        imports: [TranslocoModule, MockI18nPipe],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    const fixture = TestBed.createComponent(CategoryListSelectComponent);
    if (inputs.categories) {
      fixture.componentRef.setInput('categories', inputs.categories);
    }
    if (inputs.currentId) {
      fixture.componentRef.setInput('currentId', inputs.currentId);
    }
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should have empty categories by default', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance.categories()).toEqual([]);
  });

  it('should have undefined currentId by default', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance.currentId()).toBeUndefined();
  });

  it('should render category list items', () => {
    const categories: HelpCategory[] = [
      { id: '1', title: { en: 'Cat 1' }, numberOfArticles: 2 },
      { id: '2', title: { en: 'Cat 2' }, numberOfArticles: 0 },
    ];
    const fixture = createComponent({ categories });

    const items = fixture.nativeElement.querySelectorAll('li');
    expect(items).toHaveLength(2);
  });

  it('should mark the current category as selected', () => {
    const categories: HelpCategory[] = [
      { id: '1', title: { en: 'Cat 1' } },
      { id: '2', title: { en: 'Cat 2' } },
    ];
    const fixture = createComponent({ categories, currentId: '2' });

    const items: NodeListOf<HTMLLIElement> =
      fixture.nativeElement.querySelectorAll('li');
    expect(items[0].classList.contains('selected')).toBe(false);
    expect(items[1].classList.contains('selected')).toBe(true);
  });
});
