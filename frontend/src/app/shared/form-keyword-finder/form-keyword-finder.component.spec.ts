import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { KeywordsService } from 'app/core/generated/circabc';
import { SelectableKeyword } from 'app/core/ui-model/index';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { FormKeywordFinderComponent } from './form-keyword-finder.component';

const mockKeywords: SelectableKeyword[] = [
  { id: '1', title: { en: 'Keyword1' }, selected: false },
  { id: '2', title: { en: 'Keyword2' }, selected: false },
];

describe('FormKeywordFinderComponent', () => {
  let component: FormKeywordFinderComponent;
  let fixture: ComponentFixture<FormKeywordFinderComponent>;
  let mockKeywordsService: {
    getKeywordDefinitionsAsync: ReturnType<typeof vi.fn>;
  };
  let form: FormGroup;

  beforeEach(async () => {
    mockKeywordsService = {
      getKeywordDefinitionsAsync: vi.fn().mockResolvedValue([...mockKeywords]),
    };

    form = new FormGroup({
      keywords: new FormControl(null),
    });

    await TestBed.configureTestingModule({
      imports: [FormKeywordFinderComponent],
      providers: [
        { provide: KeywordsService, useValue: mockKeywordsService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FormKeywordFinderComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('groupId', 'group1');
    fixture.componentRef.setInput('searchAdvancedForm', form);
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  it('should load available keywords on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockKeywordsService.getKeywordDefinitionsAsync).toHaveBeenCalledWith(
      {
        id: 'group1',
      }
    );
    expect(component.availableKeywords()).toHaveLength(2);
  });

  it('should not load keywords when groupId is undefined', async () => {
    fixture.componentRef.setInput('groupId', undefined);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(
      mockKeywordsService.getKeywordDefinitionsAsync
    ).not.toHaveBeenCalled();
    expect(component.availableKeywords()).toEqual([]);
  });

  it('should toggle keyword selection', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.toggleSelected(component.availableKeywords()[0]);

    expect(component.availableKeywords()[0].selected).toBe(true);
    expect(component.availableKeywords()[1].selected).toBe(false);
    expect(form.controls['keywords'].value).toEqual([
      component.availableKeywords()[0],
    ]);
  });

  it('should deselect a previously selected keyword', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.availableKeywords()[0].selected = true;
    component.toggleSelected(component.availableKeywords()[0]);

    expect(component.availableKeywords()[0].selected).toBe(false);
    expect(form.controls['keywords'].value).toEqual([]);
  });

  it('should update availableKeywords selection from form valueChanges', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    form.controls['keywords'].setValue([
      { id: '2', title: { en: 'Keyword2' } },
    ]);

    expect(component.availableKeywords()[0].selected).toBe(false);
    expect(component.availableKeywords()[1].selected).toBe(true);
  });

  it('should reset selections when form keywords is null', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.availableKeywords()[0].selected = true;
    form.controls['keywords'].setValue(null);

    expect(component.availableKeywords()[0].selected).toBe(false);
    expect(component.availableKeywords()[1].selected).toBe(false);
  });

  it('hasKeywords should return true when keywords are loaded', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.hasKeywords()).toBe(true);
  });

  it('hasKeywords should return false when no keywords', () => {
    fixture.detectChanges();
    component.availableKeywords.set([]);

    expect(component.hasKeywords()).toBe(false);
  });
});
