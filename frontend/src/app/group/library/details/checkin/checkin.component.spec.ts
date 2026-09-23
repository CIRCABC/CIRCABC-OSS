import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ContentService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CheckinComponent } from './checkin.component';

const mockContentService = {
  putCheckin: vi.fn().mockReturnValue(of(undefined)),
  putCheckinAsync: vi.fn().mockResolvedValue(undefined),
};

describe('CheckinComponent', () => {
  let component: CheckinComponent;
  let fixture: ComponentFixture<CheckinComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CheckinComponent, ReactiveFormsModule],
      providers: [
        { provide: ContentService, useValue: mockContentService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CheckinComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('nodeId', 'test-node-id');
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.form.controls['comment'].value).toBe('');
    expect(component.form.controls['minorChange'].value).toBe(true);
    expect(component.form.controls['keepCheckedOut'].value).toBe(false);
  });

  it('should emit modalHide and reset processing on closePopupWindow', () => {
    const modalHideSpy = vi.spyOn(component.modalHide, 'emit');
    component.processing.set(true);

    component.closePopupWindow();

    expect(modalHideSpy).toHaveBeenCalled();
    expect(component.processing()).toBe(false);
  });

  it('should call putCheckin with form values and emit checkedIn', async () => {
    const checkedInSpy = vi.spyOn(component.checkedIn, 'emit');
    const modalHideSpy = vi.spyOn(component.modalHide, 'emit');

    component.form.controls['comment'].setValue('my comment');
    component.form.controls['minorChange'].setValue(false);
    component.form.controls['keepCheckedOut'].setValue(true);

    await component.checkin();

    expect(mockContentService.putCheckinAsync).toHaveBeenCalledWith({
      id: 'test-node-id',
      minorChange: false,
      keepCheckedOut: true,
      endEditInline: undefined,
      comment: 'my comment',
    });
    expect(checkedInSpy).toHaveBeenCalled();
    expect(modalHideSpy).toHaveBeenCalled();
    expect(component.processing()).toBe(false);
  });

  it('should reset form and set minorChange to true after checkin', async () => {
    component.form.controls['comment'].setValue('test');
    component.form.controls['minorChange'].setValue(false);

    await component.checkin();

    expect(component.form.controls['minorChange'].value).toBe(true);
    expect(component.form.controls['comment'].value).toBeNull();
  });
});
