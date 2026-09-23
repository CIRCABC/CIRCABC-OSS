import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { Category, CategoryService, User } from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { CategorySupportComponent } from './category-support.component';

const mockCategory: Category = {
  id: 'cat-1',
  name: 'Test Category',
  useSingleContact: true,
  contactEmails: ['admin@test.com'],
};

const mockAdmins: User[] = [
  { userId: 'user1', email: 'user1@test.com' },
  { userId: 'user2', email: 'user2@test.com' },
];

function flushMicrotasks(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

describe('CategorySupportComponent', () => {
  let component: CategorySupportComponent;
  let fixture: ComponentFixture<CategorySupportComponent>;
  let paramsSubject: Subject<{ id: string }>;

  const mockCategoryService = {
    getCategoryAsync: vi.fn().mockResolvedValue(mockCategory),
    getCategoryAdministratorsAsync: vi.fn().mockResolvedValue(mockAdmins),
    putCategory: vi.fn().mockReturnValue(of(mockCategory)),
    putCategoryAsync: vi.fn().mockResolvedValue(mockCategory),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [CategorySupportComponent],
      providers: [
        { provide: CategoryService, useValue: mockCategoryService },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CategorySupportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize category and administrators on route params', async () => {
    paramsSubject.next({ id: 'cat-1' });
    await flushMicrotasks();

    expect(mockCategoryService.getCategoryAsync).toHaveBeenCalledWith({
      id: 'cat-1',
    });
    expect(
      mockCategoryService.getCategoryAdministratorsAsync
    ).toHaveBeenCalledWith({
      id: 'cat-1',
    });
    expect(component.category).toEqual(mockCategory);
    expect(component.administrators()).toEqual(mockAdmins);
  });

  it('should set contactEmail when useSingleContact is true', async () => {
    paramsSubject.next({ id: 'cat-1' });
    await flushMicrotasks();

    expect(component.categoryForm.controls['useSingleContact'].value).toBe(
      true
    );
    expect(component.categoryForm.controls['contactEmail'].value).toBe(
      'admin@test.com'
    );
  });

  it('should set contactEmails when useSingleContact is false', async () => {
    const multiCategory: Category = {
      ...mockCategory,
      useSingleContact: false,
      contactEmails: ['a@test.com', 'b@test.com'],
    };
    mockCategoryService.getCategoryAsync.mockResolvedValue(multiCategory);

    paramsSubject.next({ id: 'cat-1' });
    await flushMicrotasks();

    expect(component.categoryForm.controls['useSingleContact'].value).toBe(
      false
    );
    expect(component.categoryForm.controls['contactEmails'].value).toBe(
      'a@test.com,b@test.com,'
    );
  });

  it('should call putCategory on update when form is valid', async () => {
    paramsSubject.next({ id: 'cat-1' });
    await flushMicrotasks();

    component.categoryForm.controls['useSingleContact'].setValue(true);
    component.categoryForm.controls['contactEmail'].setValue('new@test.com');

    await component.update();

    expect(mockCategoryService.putCategoryAsync).toHaveBeenCalledWith({
      id: 'cat-1',
      category: expect.objectContaining({
        contactEmails: ['new@test.com'],
        useSingleContact: true,
      }),
    });
    expect(component.updating()).toBe(false);
  });

  it('should reinitialize category on cancel', async () => {
    paramsSubject.next({ id: 'cat-1' });
    await flushMicrotasks();

    mockCategoryService.getCategoryAsync.mockClear();
    await component.cancel();

    expect(mockCategoryService.getCategoryAsync).toHaveBeenCalledWith({
      id: 'cat-1',
    });
  });

  it('should toggle email selection', async () => {
    paramsSubject.next({ id: 'cat-1' });
    await flushMicrotasks();

    component.categoryForm.controls['contactEmails'].setValue('');
    component.toggleSelect('user1@test.com');

    expect(component.categoryForm.controls['contactEmails'].value).toContain(
      'user1@test.com'
    );

    component.toggleSelect('user1@test.com');
    expect(
      component.categoryForm.controls['contactEmails'].value
    ).not.toContain('user1@test.com');
  });

  it('should return false for isEmailSelected with undefined', () => {
    expect(component.isEmailSelected(undefined)).toBe(false);
  });

  it('should do nothing in toggleSelect with undefined', () => {
    const before = component.categoryForm.value.contactEmails;
    component.toggleSelect(undefined);
    expect(component.categoryForm.value.contactEmails).toBe(before);
  });

  it('isFormValid should return true when useSingleContact and contactEmail is set', async () => {
    paramsSubject.next({ id: 'cat-1' });
    await flushMicrotasks();

    component.categoryForm.controls['useSingleContact'].setValue(true);
    component.categoryForm.controls['contactEmail'].setValue('valid@test.com');

    expect(component.isFormValid()).toBe(true);
  });

  it('isFormValid should return false when useSingleContact and contactEmail is empty', () => {
    component.categoryForm.controls['useSingleContact'].setValue(true);
    component.categoryForm.controls['contactEmail'].setValue('');

    expect(component.isFormValid()).toBe(false);
  });

  it('isFormValid should return true when not useSingleContact and multiple emails set', () => {
    component.categoryForm.controls['useSingleContact'].setValue(false);
    component.categoryForm.controls['contactEmails'].setValue(
      'a@test.com,b@test.com,'
    );

    expect(component.isFormValid()).toBe(true);
  });
});
