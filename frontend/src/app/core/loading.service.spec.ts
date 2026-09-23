import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { vi } from 'vitest';
import { LoadingService } from './loading.service';

const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

const mockTranslocoService = {
  translate: vi.fn((key: string) => `translated:${key}`),
};

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    vi.clearAllMocks();

    TestBed.configureTestingModule({
      providers: [
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: TranslocoService, useValue: mockTranslocoService },
      ],
    });
    service = TestBed.inject(LoadingService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should return the operation result and reset loading on success', async () => {
    const loading = signal(false);

    const result = await service.run(loading, async () => 42);

    expect(result).toBe(42);
    expect(loading()).toBe(false);
    expect(mockUiMessageService.addErrorMessage).not.toHaveBeenCalled();
  });

  it('should hold loading true while the operation is in flight', async () => {
    const loading = signal(false);
    let loadingDuringOperation = false;

    await service.run(loading, async () => {
      loadingDuringOperation = loading();
      return 'done';
    });

    expect(loadingDuringOperation).toBe(true);
    expect(loading()).toBe(false);
  });

  it('should reset loading and show a localized error toast on failure', async () => {
    const loading = signal(false);

    const result = await service.run(loading, async () => {
      throw new Error('boom');
    });

    expect(result).toBeUndefined();
    expect(loading()).toBe(false);
    expect(mockTranslocoService.translate).toHaveBeenCalledWith(
      'error.loading.data'
    );
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
      'translated:error.loading.data'
    );
  });

  it('should use the provided error key when the operation fails', async () => {
    const loading = signal(false);

    await service.run(
      loading,
      async () => {
        throw new Error('boom');
      },
      'error.keywords.read'
    );

    expect(mockTranslocoService.translate).toHaveBeenCalledWith(
      'error.keywords.read'
    );
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
      'translated:error.keywords.read'
    );
  });
});
