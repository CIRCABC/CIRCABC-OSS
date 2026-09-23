import { inject, Service, WritableSignal } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { UiMessageService } from 'app/core/message/ui-message.service';

/**
 * Root-provided helper that runs an asynchronous operation while keeping a
 * boolean "loading" signal in sync and surfacing failures to the user.
 *
 * Many components toggle a loading indicator manually:
 *
 * ```ts
 * this.loading.set(true);
 * this.data.set(await service.fetchAsync());
 * this.loading.set(false); // never reached if fetchAsync() rejects
 * ```
 *
 * When the awaited call rejects the flag is never reset, so the loader spins
 * forever and no error is shown. {@link LoadingService.run} removes that
 * failure mode: it always resets the signal in a `finally` block and shows a
 * localized error toast (via {@link UiMessageService}) when the operation
 * throws.
 *
 * Key collaborators:
 * - {@link UiMessageService}: displays the error notification.
 * - {@link TranslocoService}: resolves the localized error text.
 */
@Service()
export class LoadingService {
  /** Service used to display the error notification on failure. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Service used to resolve the localized error message. */
  private readonly translateService = inject(TranslocoService);

  /**
   * Runs {@link operation} while holding {@link loading} `true`, guaranteeing
   * the signal is reset once the operation settles.
   *
   * On success the resolved value is returned. On failure the flag is still
   * reset, a localized error message (resolved from {@link errorKey}) is shown
   * through {@link UiMessageService}, and `undefined` is returned so callers
   * can continue without an unhandled rejection leaking to the console.
   *
   * @typeParam T - The type produced by the asynchronous operation.
   * @param loading - Writable boolean signal driving the loading indicator.
   * @param operation - Factory returning the promise to await.
   * @param errorKey - Transloco key for the error message shown on failure.
   * Defaults to `'error.loading.data'`.
   * @returns The operation result, or `undefined` when it failed.
   */
  public async run<T>(
    loading: WritableSignal<boolean>,
    operation: () => Promise<T>,
    errorKey = 'error.loading.data'
  ): Promise<T | undefined> {
    loading.set(true);
    try {
      return await operation();
    } catch {
      this.uiMessageService.addErrorMessage(
        this.translateService.translate(errorKey)
      );
      return undefined;
    } finally {
      loading.set(false);
    }
  }
}
