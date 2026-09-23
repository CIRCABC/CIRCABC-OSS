import { NO_ERRORS_SCHEMA, SimpleChange, SimpleChanges } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { SearchConfig } from 'app/core/generated/circabc';
import { UserPreferencesService } from 'app/core/user-preferences.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AdvancedSearchComponent } from './advanced-search.component';

const mockUserPreferencesService = {
  getSearchConfiguration: vi.fn().mockReturnValue([] as SearchConfig[]),
  saveSearchPreferences: vi.fn(),
  deleteConfiguration: vi.fn(),
};

describe('AdvancedSearchComponent', () => {
  let component: AdvancedSearchComponent;
  let fixture: ComponentFixture<AdvancedSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdvancedSearchComponent],
      providers: [
        provideNativeDateAdapter(),
        {
          provide: UserPreferencesService,
          useValue: mockUserPreferencesService,
        },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { firstChild: null } },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: {
            getTranslation: vi.fn().mockReturnValue(of({})),
          },
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    })
      .overrideComponent(AdvancedSearchComponent, {
        set: {
          imports: [ReactiveFormsModule],
          template: '',
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(AdvancedSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should build the form on init', () => {
    expect(component.searchAdvancedForm).toBeDefined();
    expect(component.searchAdvancedForm.controls['searchString']).toBeDefined();
    expect(component.searchAdvancedForm.controls['searchIn'].value).toBe('ALL');
  });

  it('should update searchString on ngOnChanges', () => {
    const changes: SimpleChanges = {
      searchString: new SimpleChange(null, 'test query', false),
    };
    component.ngOnChanges(changes);
    expect(component.searchAdvancedForm.controls['searchString'].value).toBe(
      'test query'
    );
  });

  it('should not update searchString on ngOnChanges when value is empty', () => {
    component.searchAdvancedForm.controls['searchString'].setValue('existing');
    const changes: SimpleChanges = {
      searchString: new SimpleChange('existing', '', false),
    };
    component.ngOnChanges(changes);
    expect(component.searchAdvancedForm.controls['searchString'].value).toBe(
      'existing'
    );
  });

  it('should select a saved search configuration', () => {
    const config: SearchConfig = {
      searchName: 'mySearch',
      searchFor: 'docs',
      searchIn: SearchConfig.SearchInEnum.Library,
      language: 'en',
    };
    component.searchConfigOptions = [config];

    component.selectSearch('mySearch');

    expect(component.searchAdvancedForm.value.searchIn).toBe('library');
    expect(component.searchAdvancedForm.value.language).toBe('en');
  });

  it('should not patch form if search name not found', () => {
    component.searchConfigOptions = [];
    component.searchAdvancedForm.controls['searchIn'].setValue('ALL');

    component.selectSearch('nonexistent');

    expect(component.searchAdvancedForm.controls['searchIn'].value).toBe('ALL');
  });

  it('should save search and refresh config options', () => {
    const newConfigs: SearchConfig[] = [{ searchName: 'saved' }];
    mockUserPreferencesService.getSearchConfiguration.mockReturnValue(
      newConfigs
    );

    component.saveAsWindow = true;
    component.saveSearch();

    expect(
      mockUserPreferencesService.saveSearchPreferences
    ).toHaveBeenCalledWith(component.searchAdvancedForm.value);
    expect(component.saveAsWindow).toBe(false);
    expect(component.searchConfigOptions).toEqual(newConfigs);
  });

  it('should delegate deleteConfiguration to service', () => {
    component.deleteConfiguration('mySearch');
    expect(mockUserPreferencesService.deleteConfiguration).toHaveBeenCalledWith(
      'mySearch'
    );
  });

  it('should emit advancedSearch with form data on search()', async () => {
    const emitSpy = vi.spyOn(component.advancedSearch, 'emit');
    component.searchAdvancedForm.controls['searchString'].setValue('hello');

    await component.search();

    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({ searchString: 'hello' })
    );
  });

  it('should emit keywords formatted as bracket-wrapped string', async () => {
    const emitSpy = vi.spyOn(component.advancedSearch, 'emit');
    component.searchAdvancedForm.controls['searchString'].setValue('test');
    component.searchAdvancedForm.controls['keywords'].setValue([
      { id: 'kw1' },
      { id: 'kw2' },
    ]);

    await component.search();

    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({ keywords: '[kw1, kw2]' })
    );
  });

  it('should return searchString control from getter', () => {
    expect(component.searchStringControl).toBe(
      component.searchAdvancedForm.controls['searchString']
    );
  });
});
