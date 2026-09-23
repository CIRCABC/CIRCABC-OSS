import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { NotificationService } from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { PasteNotificationsComponent } from './paste-notifications.component';

describe('PasteNotificationsComponent', () => {
  let component: PasteNotificationsComponent;
  let fixture: ComponentFixture<PasteNotificationsComponent>;

  const paramsSubject = new Subject<{ [key: string]: string }>();

  const mockNotificationService = {
    getPasteNotificationsAsync: vi
      .fn()
      .mockResolvedValue({ pasteEnabled: true, pasteAllEnabled: false }),
    postPasteNotificationsAsync: vi.fn().mockResolvedValue(undefined),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasteNotificationsComponent, ReactiveFormsModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: NotificationService, useValue: mockNotificationService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PasteNotificationsComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  it('should load paste notification state on route params change', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig123' });
    await fixture.whenStable();

    expect(component.igId).toBe('ig123');
    expect(
      mockNotificationService.getPasteNotificationsAsync
    ).toHaveBeenCalledWith({
      id: 'ig123',
    });
    expect(component.pasteNotificationsForm.controls['notifyPaste'].value).toBe(
      true
    );
    expect(
      component.pasteNotificationsForm.controls['notifyPasteAll'].value
    ).toBe(false);
  });

  it('should save the form values', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig456' });
    await fixture.whenStable();

    component.pasteNotificationsForm.controls['notifyPaste'].setValue(false);
    component.pasteNotificationsForm.controls['notifyPasteAll'].setValue(true);

    await component.save();

    expect(
      mockNotificationService.postPasteNotificationsAsync
    ).toHaveBeenCalledWith({
      id: 'ig456',
      pasteEnable: false,
      pasteAllEnable: true,
    });
    expect(component.saving()).toBe(false);
  });

  it('should set saving to true during save', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig789' });
    await fixture.whenStable();

    const savePromise = component.save();
    expect(component.saving()).toBe(true);

    await savePromise;
    expect(component.saving()).toBe(false);
  });

  it('should reload state on cancel', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig123' });
    await fixture.whenStable();

    vi.clearAllMocks();
    mockNotificationService.getPasteNotificationsAsync.mockResolvedValue({
      pasteEnabled: false,
      pasteAllEnabled: true,
    });

    await component.cancel();

    expect(
      mockNotificationService.getPasteNotificationsAsync
    ).toHaveBeenCalledWith({
      id: 'ig123',
    });
    expect(component.pasteNotificationsForm.controls['notifyPaste'].value).toBe(
      false
    );
    expect(
      component.pasteNotificationsForm.controls['notifyPasteAll'].value
    ).toBe(true);
  });
});
