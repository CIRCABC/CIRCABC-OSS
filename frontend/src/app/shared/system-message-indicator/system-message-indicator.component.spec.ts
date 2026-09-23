import { TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AppMessage, AppMessageService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { SystemMessageIndicatorComponent } from './system-message-indicator.component';

describe('SystemMessageIndicatorComponent', () => {
  const mockAppMessageService = {
    getEnabledAppMessagesAsync: vi.fn().mockResolvedValue([]),
  };

  const mockUiMessageService = {
    addInfoMessage: vi.fn(),
    addErrorMessage: vi.fn(),
    addWarningMessage: vi.fn(),
    addSuccessMessage: vi.fn(),
  };

  function createComponent() {
    const fixture = TestBed.createComponent(SystemMessageIndicatorComponent);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue([]);

    TestBed.configureTestingModule({
      imports: [SystemMessageIndicatorComponent],
      providers: [
        { provide: AppMessageService, useValue: mockAppMessageService },
        { provide: UiMessageService, useValue: mockUiMessageService },
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
    const fixture = createComponent();
    await fixture.whenStable();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should fetch messages on init', async () => {
    const messages: AppMessage[] = [
      { id: 1, content: 'Test', level: 'info', enabled: true, displayTime: 20 },
    ];
    mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
      messages
    );

    const fixture = createComponent();
    await fixture.whenStable();

    expect(fixture.componentInstance.listOfMessages()).toEqual(messages);
  });

  describe('hasMessages', () => {
    it('should return false when no messages', async () => {
      const fixture = createComponent();
      await fixture.whenStable();
      expect(fixture.componentInstance.hasMessages()).toBe(false);
    });

    it('should return true when there are enabled messages without passed closure date', async () => {
      const futureDate = new Date();
      futureDate.setFullYear(futureDate.getFullYear() + 1);
      const messages: AppMessage[] = [
        {
          id: 1,
          content: 'Msg',
          level: 'info',
          enabled: true,
          displayTime: 20,
          dateClosure: futureDate.toISOString(),
        },
      ];
      mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
        messages
      );

      const fixture = createComponent();
      await fixture.whenStable();

      expect(fixture.componentInstance.hasMessages()).toBe(true);
    });

    it('should return false when message closure date has passed', async () => {
      const pastDate = new Date();
      pastDate.setFullYear(pastDate.getFullYear() - 1);
      const messages: AppMessage[] = [
        {
          id: 1,
          content: 'Msg',
          level: 'info',
          enabled: true,
          displayTime: 20,
          dateClosure: pastDate.toISOString(),
        },
      ];
      mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
        messages
      );

      const fixture = createComponent();
      await fixture.whenStable();

      expect(fixture.componentInstance.hasMessages()).toBe(false);
    });

    it('should return false when message is disabled', async () => {
      const messages: AppMessage[] = [
        {
          id: 1,
          content: 'Msg',
          level: 'info',
          enabled: false,
          displayTime: 20,
        },
      ];
      mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
        messages
      );

      const fixture = createComponent();
      await fixture.whenStable();

      expect(fixture.componentInstance.hasMessages()).toBe(false);
    });
  });

  describe('displayMessages on init', () => {
    it('should display messages on first load and set localStorage flag', async () => {
      const messages: AppMessage[] = [
        {
          id: 1,
          content: 'Hello',
          level: 'info',
          enabled: true,
          displayTime: 20,
        },
      ];
      mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
        messages
      );

      const fixture = createComponent();
      await fixture.whenStable();
      fixture.detectChanges();
      await fixture.whenStable();

      expect(localStorage.getItem('systemMessageAlreadyShown')).toBe('1');
      expect(mockUiMessageService.addInfoMessage).toHaveBeenCalledWith(
        'Hello',
        false,
        30
      );
    });

    it('should not display messages if localStorage flag is already set', async () => {
      localStorage.setItem('systemMessageAlreadyShown', '1');
      const messages: AppMessage[] = [
        {
          id: 1,
          content: 'Hello',
          level: 'info',
          enabled: true,
          displayTime: 20,
        },
      ];
      mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
        messages
      );

      const fixture = createComponent();
      await fixture.whenStable();

      expect(mockUiMessageService.addInfoMessage).not.toHaveBeenCalled();
    });
  });

  describe('forceDisplayMessages', () => {
    it('should display error messages with persist=true', async () => {
      const messages: AppMessage[] = [
        {
          id: 1,
          content: 'Error!',
          level: 'error',
          enabled: true,
          displayTime: 20,
        },
      ];
      mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
        messages
      );
      localStorage.setItem('systemMessageAlreadyShown', '1');

      const fixture = createComponent();
      await fixture.whenStable();

      fixture.componentInstance.forceDisplayMessages();

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
        'Error!',
        true,
        20
      );
    });

    it('should display warning messages', async () => {
      const messages: AppMessage[] = [
        {
          id: 1,
          content: 'Warn',
          level: 'warning',
          enabled: true,
          displayTime: 25,
        },
      ];
      mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
        messages
      );
      localStorage.setItem('systemMessageAlreadyShown', '1');

      const fixture = createComponent();
      await fixture.whenStable();

      fixture.componentInstance.forceDisplayMessages();

      expect(mockUiMessageService.addWarningMessage).toHaveBeenCalledWith(
        'Warn',
        true,
        25
      );
    });

    it('should use addSuccessMessage for unknown level', async () => {
      const messages: AppMessage[] = [
        {
          id: 1,
          content: 'Ok',
          level: 'unknown',
          enabled: true,
          displayTime: 20,
        },
      ];
      mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
        messages
      );
      localStorage.setItem('systemMessageAlreadyShown', '1');

      const fixture = createComponent();
      await fixture.whenStable();

      fixture.componentInstance.forceDisplayMessages();

      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalledWith(
        'Ok',
        true,
        20
      );
    });

    it('should enforce minimum displayTime of 15 when value is less', async () => {
      const messages: AppMessage[] = [
        {
          id: 1,
          content: 'Fast',
          level: 'info',
          enabled: true,
          displayTime: 5,
        },
      ];
      mockAppMessageService.getEnabledAppMessagesAsync.mockResolvedValue(
        messages
      );
      localStorage.setItem('systemMessageAlreadyShown', '1');

      const fixture = createComponent();
      await fixture.whenStable();

      fixture.componentInstance.forceDisplayMessages();

      expect(mockUiMessageService.addInfoMessage).toHaveBeenCalledWith(
        'Fast',
        true,
        15
      );
    });
  });

  it('should clear interval on destroy', async () => {
    const clearIntervalSpy = vi.spyOn(globalThis, 'clearInterval');

    const fixture = createComponent();
    await fixture.whenStable();

    fixture.destroy();

    expect(clearIntervalSpy).toHaveBeenCalled();
    clearIntervalSpy.mockRestore();
  });
});
