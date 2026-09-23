import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  AppMessageService,
  PagedAppMessages,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { SystemMessageComponent } from './system-message.component';

const mockPagedMessages: PagedAppMessages = {
  data: [
    { id: 1, content: 'msg1', level: 'info', enabled: true, displayTime: 5 },
  ],
  total: 1,
};

const mockAppMessageService = {
  getPagedAppMessagesTemplateAsync: vi
    .fn()
    .mockResolvedValue(mockPagedMessages),
  // The template also renders <cbc-old-ui-configuration>, which injects the
  // same AppMessageService to load/persist its own settings.
  getDisplayOldMessageAsync: vi.fn().mockResolvedValue({ display: true }),
  getEnableOldMessageAsync: vi.fn().mockResolvedValue({ enable: false }),
  setDisplayOldMessageAsync: vi.fn().mockResolvedValue(undefined),
  setEnableOldMessageAsync: vi.fn().mockResolvedValue(undefined),
};

describe('SystemMessageComponent', () => {
  let component: SystemMessageComponent;
  let fixture: ComponentFixture<SystemMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SystemMessageComponent],
      providers: [
        { provide: AppMessageService, useValue: mockAppMessageService },
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    mockAppMessageService.getPagedAppMessagesTemplateAsync.mockResolvedValue(
      mockPagedMessages
    );

    fixture = TestBed.createComponent(SystemMessageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load templates on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(
      mockAppMessageService.getPagedAppMessagesTemplateAsync
    ).toHaveBeenCalledWith({ limit: 5, page: 1 });
    expect(component.templates()).toEqual(mockPagedMessages);
    expect(component.totalItems()).toBe(1);
    expect(component.loading()).toBe(false);
  });

  it('should update page and reload templates on goToPage', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    mockAppMessageService.getPagedAppMessagesTemplateAsync.mockClear();

    component.goToPage(3);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.listingOptions().page).toBe(3);
    expect(
      mockAppMessageService.getPagedAppMessagesTemplateAsync
    ).toHaveBeenCalledWith({ limit: 5, page: 3 });
  });

  it('should handle error when loading templates fails', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    mockAppMessageService.getPagedAppMessagesTemplateAsync.mockRejectedValue(
      new Error('fail')
    );

    fixture.detectChanges();
    await fixture.whenStable();

    expect(consoleSpy).toHaveBeenCalledWith(
      'problem getting the list of templates'
    );
    expect(component.loading()).toBe(false);
    consoleSpy.mockRestore();
  });

  it('should reset showDeleteModal and reload on refresh', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.showDeleteModal = true;
    mockAppMessageService.getPagedAppMessagesTemplateAsync.mockClear();

    component.refresh();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.showDeleteModal).toBe(false);
    expect(
      mockAppMessageService.getPagedAppMessagesTemplateAsync
    ).toHaveBeenCalled();
  });
});
