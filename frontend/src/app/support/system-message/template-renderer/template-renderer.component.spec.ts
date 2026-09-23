import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AppMessage, AppMessageService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { TemplateRendererComponent } from './template-renderer.component';

const mockTemplate: AppMessage = {
  id: 42,
  content: 'Test message',
  level: 'INFO',
  enabled: true,
  displayTime: 5000,
};

describe('TemplateRendererComponent', () => {
  let fixture: ComponentFixture<TemplateRendererComponent>;
  let componentRef: ComponentRef<TemplateRendererComponent>;
  let mockAppMessageService: {
    deleteAppMessageTemplate: ReturnType<typeof vi.fn>;
    deleteAppMessageTemplateAsync: ReturnType<typeof vi.fn>;
    setOldMessage: ReturnType<typeof vi.fn>;
    setOldMessageAsync: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockAppMessageService = {
      deleteAppMessageTemplate: vi.fn().mockReturnValue(of(undefined)),
      deleteAppMessageTemplateAsync: vi.fn().mockResolvedValue(undefined),
      setOldMessage: vi.fn().mockReturnValue(of(undefined)),
      setOldMessageAsync: vi.fn().mockResolvedValue(undefined),
    };

    TestBed.configureTestingModule({
      imports: [TemplateRendererComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: AppMessageService, useValue: mockAppMessageService },
      ],
    });

    fixture = TestBed.createComponent(TemplateRendererComponent);
    componentRef = fixture.componentRef;
    componentRef.setInput('template', mockTemplate);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeDefined();
  });

  describe('onDelete', () => {
    it('should call deleteAppMessageTemplate and emit delete', async () => {
      const deleteSpy = vi.fn();
      fixture.componentInstance.delete.subscribe(deleteSpy);

      await fixture.componentInstance.onDelete();

      expect(
        mockAppMessageService.deleteAppMessageTemplateAsync
      ).toHaveBeenCalledWith({ id: '42' });
      expect(deleteSpy).toHaveBeenCalled();
    });

    it('should not call service if template has no id', async () => {
      componentRef.setInput('template', { ...mockTemplate, id: 0 });
      fixture.detectChanges();

      await fixture.componentInstance.onDelete();

      expect(
        mockAppMessageService.deleteAppMessageTemplateAsync
      ).not.toHaveBeenCalled();
    });

    it('should emit delete even if service throws', async () => {
      mockAppMessageService.deleteAppMessageTemplateAsync.mockRejectedValue(
        new Error('fail')
      );
      const deleteSpy = vi.fn();
      fixture.componentInstance.delete.subscribe(deleteSpy);

      await fixture.componentInstance.onDelete();

      expect(deleteSpy).toHaveBeenCalled();
    });
  });

  describe('useAsOldMessage', () => {
    it('should call setOldMessage with the template', async () => {
      await fixture.componentInstance.useAsOldMessage();

      expect(mockAppMessageService.setOldMessageAsync).toHaveBeenCalledWith({
        appMessage: mockTemplate,
      });
    });

    it('should handle error without throwing', async () => {
      mockAppMessageService.setOldMessageAsync.mockRejectedValue(
        new Error('fail')
      );

      await expect(
        fixture.componentInstance.useAsOldMessage()
      ).resolves.toBeUndefined();
    });
  });
});
