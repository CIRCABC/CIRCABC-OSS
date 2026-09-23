import { TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AppMessageService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { OldUiConfigurationComponent } from './old-ui-configuration.component';

const mockAppMessageService = {
  getDisplayOldMessageAsync: vi.fn().mockResolvedValue({ display: true }),
  getEnableOldMessageAsync: vi.fn().mockResolvedValue({ enable: false }),
  setDisplayOldMessage: vi.fn().mockReturnValue(of(undefined)),
  setDisplayOldMessageAsync: vi.fn().mockResolvedValue(undefined),
  setEnableOldMessage: vi.fn().mockReturnValue(of(undefined)),
  setEnableOldMessageAsync: vi.fn().mockResolvedValue(undefined),
};

describe('OldUiConfigurationComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockAppMessageService.getDisplayOldMessageAsync.mockResolvedValue({
      display: true,
    });
    mockAppMessageService.getEnableOldMessageAsync.mockResolvedValue({
      enable: false,
    });
    mockAppMessageService.setDisplayOldMessage.mockReturnValue(of(undefined));
    mockAppMessageService.setDisplayOldMessageAsync.mockResolvedValue(
      undefined
    );
    mockAppMessageService.setEnableOldMessage.mockReturnValue(of(undefined));
    mockAppMessageService.setEnableOldMessageAsync.mockResolvedValue(undefined);

    TestBed.configureTestingModule({
      imports: [OldUiConfigurationComponent],
      providers: [
        { provide: AppMessageService, useValue: mockAppMessageService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });
  });

  it('should create', async () => {
    const fixture = TestBed.createComponent(OldUiConfigurationComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should initialize form with values from service', async () => {
    const fixture = TestBed.createComponent(OldUiConfigurationComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const component = fixture.componentInstance;
    expect(component.form.controls['display'].value).toBe(true);
    expect(component.form.controls['enableOld'].value).toBe(false);
  });

  it('should call setDisplayOldMessage when display value changes', async () => {
    const fixture = TestBed.createComponent(OldUiConfigurationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    mockAppMessageService.setDisplayOldMessageAsync.mockClear();
    await fixture.componentInstance.updateSettings(false);

    expect(
      mockAppMessageService.setDisplayOldMessageAsync
    ).toHaveBeenCalledWith({ body: false });
  });

  it('should call setEnableOldMessage when enableOld value changes', async () => {
    const fixture = TestBed.createComponent(OldUiConfigurationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    mockAppMessageService.setEnableOldMessageAsync.mockClear();
    await fixture.componentInstance.toggleOldMessage(true);

    expect(mockAppMessageService.setEnableOldMessageAsync).toHaveBeenCalledWith(
      {
        body: true,
      }
    );
  });

  it('should not call setDisplayOldMessage when value is undefined', async () => {
    const fixture = TestBed.createComponent(OldUiConfigurationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    mockAppMessageService.setDisplayOldMessageAsync.mockClear();
    await fixture.componentInstance.updateSettings(
      undefined as unknown as boolean
    );

    expect(
      mockAppMessageService.setDisplayOldMessageAsync
    ).not.toHaveBeenCalled();
  });

  it('should not call setEnableOldMessage when value is undefined', async () => {
    const fixture = TestBed.createComponent(OldUiConfigurationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    mockAppMessageService.setEnableOldMessageAsync.mockClear();
    await fixture.componentInstance.toggleOldMessage(
      undefined as unknown as boolean
    );

    expect(
      mockAppMessageService.setEnableOldMessageAsync
    ).not.toHaveBeenCalled();
  });
});
