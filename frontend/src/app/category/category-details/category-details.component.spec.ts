import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionService } from 'app/action-result/action.service';
import { CategoryService, NodesService } from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { CategoryDetailsComponent } from './category-details.component';

describe('CategoryDetailsComponent', () => {
  let component: CategoryDetailsComponent;
  let fixture: ComponentFixture<CategoryDetailsComponent>;

  const paramsSubject = new Subject<{ id: string }>();

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue({
      id: 'cat-1',
      name: 'Test Category',
      title: { en: 'Test' },
    }),
  };

  const mockCategoryService = {
    putCategory: vi.fn().mockReturnValue(of({})),
    putCategoryAsync: vi.fn().mockResolvedValue({}),
  };

  const mockActionService = {
    propagateActionFinished: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryDetailsComponent, ReactiveFormsModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: NodesService, useValue: mockNodesService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: ActionService, useValue: mockActionService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryDetailsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should initialize form with required name validator', () => {
    fixture.detectChanges();
    expect(component.categoryForm).toBeDefined();
    expect(component.nameControl.hasError('required')).toBe(true);
  });

  it('should load category when route params emit', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat-1' });
    await vi.waitFor(() => {
      expect(component.loading()).toBe(false);
    });
    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({ id: 'cat-1' });
    expect(component.category).toEqual({
      id: 'cat-1',
      name: 'Test Category',
      title: { en: 'Test' },
    });
    expect(component.categoryForm.value.name).toBe('Test Category');
  });

  it('should set loading to false on error', async () => {
    mockNodesService.getNodeAsync.mockReturnValueOnce(
      new (await import('rxjs')).Observable((subscriber) => {
        subscriber.error(new Error('fail'));
      })
    );
    fixture.detectChanges();
    paramsSubject.next({ id: 'bad-id' });
    await vi.waitFor(() => {
      expect(component.loading()).toBe(false);
    });
  });

  it('should call putCategory and propagateActionFinished on update', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat-1' });
    await vi.waitFor(() => {
      expect(component.loading()).toBe(false);
    });

    component.categoryForm.patchValue({
      name: 'Updated',
      title: { en: 'Updated' },
    });
    await component.update();

    expect(mockCategoryService.putCategoryAsync).toHaveBeenCalledWith({
      id: 'cat-1',
      category: expect.objectContaining({ name: 'Updated' }),
    });
    expect(mockActionService.propagateActionFinished).toHaveBeenCalled();
  });

  it('should reload category on cancel', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat-1' });
    await vi.waitFor(() => {
      expect(component.loading()).toBe(false);
    });

    mockNodesService.getNodeAsync.mockClear();
    await component.cancel();
    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({ id: 'cat-1' });
  });
});
