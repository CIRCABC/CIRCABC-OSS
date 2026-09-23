import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, provideRouter } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  Category,
  CategoryService,
  HeaderService,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { RichTextEditorComponent } from 'app/shared/rich-text-editor/rich-text-editor.component';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ContactCategoryComponent } from './contact-category.component';

describe('ContactCategoryComponent', () => {
  const mockHeaders = [
    { id: 'h1', name: 'Header 1' },
    { id: 'h2', name: 'Header 2' },
  ];

  const mockCategories: Category[] = [
    { id: 'c1', name: 'Category 1', title: { en: 'Cat One' } },
    { id: 'c2', name: 'Category 2' },
  ];

  const queryParams$ = new Subject<Record<string, string>>();

  const mockHeaderService = {
    getHeadersAsync: vi.fn().mockResolvedValue(mockHeaders),
    getCategoriesByHeaderIdAsync: vi.fn().mockResolvedValue(mockCategories),
  };

  const mockCategoryService = {
    contactCategoryAdminsAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockI18nPipe = {
    transform: vi.fn(
      (mltext: { [key: string]: string }) => Object.values(mltext)[0] ?? ''
    ),
  };

  let component: ContactCategoryComponent;
  let fixture: ComponentFixture<ContactCategoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContactCategoryComponent],
      providers: [
        { provide: HeaderService, useValue: mockHeaderService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { queryParams: queryParams$.asObservable() },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(ContactCategoryComponent, {
        set: {
          imports: [
            ReactiveFormsModule,
            RichTextEditorComponent,
            TranslocoModule,
          ],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(ContactCategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load headers on init', () => {
    expect(mockHeaderService.getHeadersAsync).toHaveBeenCalled();
    expect(component.headers()).toEqual(mockHeaders);
  });

  it('should load categories when header value changes', async () => {
    component.form.controls['header'].setValue('h1');
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockHeaderService.getCategoriesByHeaderIdAsync).toHaveBeenCalledWith(
      { id: 'h1' }
    );
    expect(component.categories()).toEqual(mockCategories);
  });

  it('should derive selectedCategory when category value changes', async () => {
    component.form.controls['header'].setValue('h1');
    fixture.detectChanges();
    await fixture.whenStable();

    component.form.controls['category'].setValue('c1');
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.selectedCategory()).toEqual(mockCategories[0]);
  });

  it('should set form values from query params', () => {
    queryParams$.next({ header: 'h2', category: 'c2' });
    expect(component.form.controls['header'].value).toBe('h2');
    expect(component.form.controls['category'].value).toBe('c2');
  });

  it('should return title via i18nPipe when title has keys', () => {
    const result = component.getNameOrTitle(mockCategories[0]);
    expect(mockI18nPipe.transform).toHaveBeenCalledWith({ en: 'Cat One' });
    expect(result).toBe('Cat One');
  });

  it('should return name when title is undefined', () => {
    const result = component.getNameOrTitle(mockCategories[1]);
    expect(result).toBe('Category 2');
  });

  it('should call contactCategoryAdmins and set processing', async () => {
    component.form.controls['category'].setValue('c1');
    component.form.controls['messageContent'].setValue('Hello');
    component.form.controls['sendCopy'].setValue(true);

    await component.contact();

    expect(mockCategoryService.contactCategoryAdminsAsync).toHaveBeenCalledWith(
      {
        id: 'c1',
        adminContactRequest: {
          content: component.form.value.messageContent,
          sendCopy: true,
        },
      }
    );
    expect(component.processing()).toBe(false);
  });

  it('should handle error in contact and reset processing', async () => {
    mockCategoryService.contactCategoryAdminsAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    component.form.controls['category'].setValue('c1');
    component.form.controls['messageContent'].setValue('test');

    await component.contact();

    expect(component.processing()).toBe(false);
    expect(consoleSpy).toHaveBeenCalled();
    consoleSpy.mockRestore();
  });
});
