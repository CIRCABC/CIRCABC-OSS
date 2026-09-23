import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { Header, HeaderService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddHeaderComponent } from './add-header.component';

describe('AddHeaderComponent', () => {
  let component: AddHeaderComponent;
  let fixture: ComponentFixture<AddHeaderComponent>;

  const mockHeaderService = {
    postHeader: vi.fn().mockReturnValue(of({ name: 'New Header' } as Header)),
    postHeaderAsync: vi
      .fn()
      .mockResolvedValue({ name: 'New Header' } as Header),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddHeaderComponent, ReactiveFormsModule],
      providers: [
        { provide: HeaderService, useValue: mockHeaderService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with empty values', () => {
    expect(component.headerForm).toBeDefined();
    expect(component.nameControl.value).toBe('');
    expect(component.descriptionControl.value).toBe('');
  });

  it('should mark form invalid when name is empty', () => {
    component.headerForm.controls['name'].setValue('');
    component.headerForm.controls['description'].setValue('some desc');
    expect(component.headerForm.valid).toBe(false);
  });

  it('should have required validator on description control', () => {
    expect(component.descriptionControl).toBeDefined();
    expect(component.descriptionControl.validator).toBeTruthy();
  });

  it('should mark form valid when both fields are filled and name is unique', () => {
    component.headerForm.controls['name'].setValue('Unique Header');
    component.headerForm.controls['description'].setValue('A description');
    expect(component.headerForm.valid).toBe(true);
  });

  it('should invalidate name if it already exists in headers', () => {
    fixture.componentRef.setInput('headers', [{ name: 'Existing' }]);
    fixture.detectChanges();

    component.headerForm.controls['name'].setValue('Existing');
    expect(component.nameControl.errors).toEqual({
      forbiddenNameArray: { name: 'Existing' },
    });
  });

  it('should set showModal to false on cancel', () => {
    component.showModal.set(true);
    component.cancel();
    expect(component.showModal()).toBe(false);
  });

  it('should not call postHeader if form is invalid', async () => {
    component.headerForm.controls['name'].setValue('');
    await component.addHeader();
    expect(mockHeaderService.postHeaderAsync).not.toHaveBeenCalled();
  });

  it('should call postHeader and emit when form is valid', async () => {
    const emitSpy = vi.spyOn(component.showModalChange, 'emit');
    component.headerForm.controls['name'].setValue('New');
    component.headerForm.controls['description'].setValue('Desc');

    await component.addHeader();

    expect(mockHeaderService.postHeaderAsync).toHaveBeenCalledWith(
      expect.objectContaining({
        header: expect.objectContaining({ name: 'New' }),
      })
    );
    expect(emitSpy).toHaveBeenCalled();
    expect(component.processing()).toBe(false);
  });

  it('should set processing to false even if postHeader throws', async () => {
    mockHeaderService.postHeaderAsync.mockRejectedValueOnce(new Error('fail'));

    component.headerForm.controls['name'].setValue('New');
    component.headerForm.controls['description'].setValue('Desc');

    try {
      await component.addHeader();
    } catch {
      // expected
    }

    expect(component.processing()).toBe(false);
  });
});
