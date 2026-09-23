import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { AppMessageService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateTemplateComponent } from './create-template.component';

const mockRouter = {
  navigate: vi.fn().mockResolvedValue(true),
};

const mockAppMessageService = {
  getAppMessageTemplateAsync: vi.fn(),
  addAppMessageTemplate: vi.fn(),
  addAppMessageTemplateAsync: vi.fn().mockResolvedValue({}),
  updateAppMessageTemplate: vi.fn(),
  updateAppMessageTemplateAsync: vi.fn().mockResolvedValue({}),
};

async function setup(params: Record<string, string> = {}) {
  const mockRoute = { params: of(params) };

  TestBed.configureTestingModule({
    imports: [CreateTemplateComponent],
    providers: [
      provideNativeDateAdapter(),
      { provide: ActivatedRoute, useValue: mockRoute },
      { provide: Router, useValue: mockRouter },
      { provide: AppMessageService, useValue: mockAppMessageService },
      provideTransloco({
        config: { defaultLang: 'en', availableLangs: ['en'] },
      }),
      {
        provide: TRANSLOCO_LOADER,
        useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
      },
    ],
  }).overrideComponent(CreateTemplateComponent, {
    set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
  });

  const fixture = TestBed.createComponent(CreateTemplateComponent);
  fixture.detectChanges();
  await fixture.whenStable();

  return { fixture, component: fixture.componentInstance };
}

describe('CreateTemplateComponent', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', async () => {
    const { component } = await setup();
    expect(component).toBeDefined();
  });

  it('should initialize form with defaults', async () => {
    const { component } = await setup();
    expect(component.templateForm.value).toEqual({
      id: '',
      content: '',
      displayTime: 15,
      dateClosure: '',
      level: 'info',
      enabled: false,
      notification: false,
    });
  });

  it('should not be in update mode without route id', async () => {
    const { component } = await setup();
    expect(component.updateMode()).toBe(false);
  });

  describe('edit mode', () => {
    const templateData = {
      id: 42,
      content: '<p>System maintenance</p>',
      displayTime: 30,
      dateClosure: '2026-05-01T00:00:00.000Z',
      level: 'warning',
      enabled: true,
    };

    it('should load template and set updateMode', async () => {
      mockAppMessageService.getAppMessageTemplateAsync.mockResolvedValue(
        templateData
      );
      const { component } = await setup({ id: '42' });

      expect(component.updateMode()).toBe(true);
      expect(component.templateForm.value.content).toBe(
        '<p>System maintenance</p>'
      );
      expect(component.templateForm.value.displayTime).toBe(30);
      expect(component.templateForm.value.level).toBe('warning');
      expect(component.templateForm.value.enabled).toBe(true);
    });
  });

  describe('isEnabled', () => {
    it('should return false by default', async () => {
      const { component } = await setup();
      expect(component.isEnabled()).toBe(false);
    });

    it('should return true when enabled is set', async () => {
      const { component } = await setup();
      component.templateForm.controls['enabled'].setValue(true);
      expect(component.isEnabled()).toBe(true);
    });
  });

  describe('isFormValid', () => {
    it('should return false when content is empty', async () => {
      const { component } = await setup();
      expect(component.isFormValid()).toBe(false);
    });

    it('should return true when content is filled', async () => {
      const { component } = await setup();
      component.templateForm.controls['content'].setValue('Some message');
      expect(component.isFormValid()).toBe(true);
    });
  });

  describe('contentControl', () => {
    it('should return the content form control', async () => {
      const { component } = await setup();
      expect(component.contentControl).toBe(
        component.templateForm.controls['content']
      );
    });
  });

  describe('cleanClosure', () => {
    it('should reset dateClosure to empty string', async () => {
      const { component } = await setup();
      component.templateForm.controls['dateClosure'].setValue(new Date());
      component.cleanClosure();
      expect(component.templateForm.value.dateClosure).toBe('');
    });
  });

  describe('isNotified', () => {
    it('should return false by default', async () => {
      const { component } = await setup();
      expect(component.isNotified()).toBe(false);
    });

    it('should return true when notification is set', async () => {
      const { component } = await setup();
      component.templateForm.controls['notification'].setValue(true);
      expect(component.isNotified()).toBe(true);
    });
  });

  describe('checkNotification', () => {
    it('should set notification to false when enabled is false', async () => {
      const { component } = await setup();
      component.templateForm.controls['notification'].setValue(true);
      component.templateForm.controls['enabled'].setValue(false);
      component.checkNotification();
      expect(component.templateForm.value.notification).toBe(false);
    });

    it('should not change notification when enabled is true', async () => {
      const { component } = await setup();
      component.templateForm.controls['enabled'].setValue(true);
      component.templateForm.controls['notification'].setValue(true);
      component.checkNotification();
      expect(component.templateForm.value.notification).toBe(true);
    });
  });

  describe('saveOrUpdateTemplate', () => {
    it('should call addAppMessageTemplate in create mode and navigate', async () => {
      mockAppMessageService.addAppMessageTemplateAsync.mockResolvedValue({});
      const { component } = await setup();
      component.templateForm.controls['content'].setValue('New message');

      await component.saveOrUpdateTemplate();

      expect(
        mockAppMessageService.addAppMessageTemplateAsync
      ).toHaveBeenCalledWith({
        appMessage: expect.objectContaining({ content: 'New message' }),
        notification: false,
      });
      expect(mockRouter.navigate).toHaveBeenCalledWith([
        'support',
        'system-message',
      ]);
      expect(component.processing()).toBe(false);
    });

    it('should call updateAppMessageTemplate in update mode and navigate', async () => {
      const templateData = {
        id: 42,
        content: '<p>Old</p>',
        displayTime: 15,
        dateClosure: '',
        level: 'info',
        enabled: false,
      };
      mockAppMessageService.getAppMessageTemplateAsync.mockResolvedValue(
        templateData
      );
      mockAppMessageService.updateAppMessageTemplateAsync.mockResolvedValue({});
      const { component } = await setup({ id: '42' });
      component.templateForm.controls['content'].setValue('Updated');

      await component.saveOrUpdateTemplate();

      expect(
        mockAppMessageService.updateAppMessageTemplateAsync
      ).toHaveBeenCalledWith({
        id: 42,
        appMessage: expect.objectContaining({ content: 'Updated' }),
        notification: false,
      });
      expect(mockRouter.navigate).toHaveBeenCalledWith([
        'support',
        'system-message',
      ]);
    });

    it('should handle errors without navigating', async () => {
      mockAppMessageService.addAppMessageTemplateAsync.mockRejectedValue(
        new Error('fail')
      );
      const { component } = await setup();
      component.templateForm.controls['content'].setValue('msg');

      await component.saveOrUpdateTemplate();

      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(component.processing()).toBe(false);
    });
  });

  describe('cancel', () => {
    it('should navigate to system-message', async () => {
      const { component } = await setup();
      await component.cancel();
      expect(mockRouter.navigate).toHaveBeenCalledWith([
        'support',
        'system-message',
      ]);
    });
  });
});
