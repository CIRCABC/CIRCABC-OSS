import { inject, Service } from '@angular/core';
import { User, UserService } from 'app/core/generated/circabc';

/**
 * Application-wide, root-provided service that caches {@link User} lookups.
 *
 * Its responsibility is to avoid redundant HTTP requests for the same user by
 * memoizing the in-flight/resolved promise returned for each user id. This lets
 * multiple callers requesting the same user share a single backend round-trip.
 *
 * Key collaborators:
 * - {@link UserService} (generated CIRCABC API client) — performs the actual
 *   `getUser` HTTP request.
 *
 * Caching semantics: promises are cached as soon as the request starts. If a
 * request rejects, its entry is evicted so a subsequent call can retry.
 */
@Service()
export class UserCacheService {
  /** Generated CIRCABC API client used to fetch users from the backend. */
  private readonly userService = inject(UserService);

  /**
   * In-memory cache mapping a user id to the (possibly still pending) promise
   * of its {@link User}. Storing the promise (rather than the resolved value)
   * deduplicates concurrent requests for the same id.
   */
  private readonly cache = new Map<string, Promise<User>>();

  /**
   * Returns the {@link User} for the given id, using the cache when possible.
   *
   * On a cache miss, a new request is issued via {@link UserService.getUser},
   * its promise is stored immediately so concurrent callers reuse it, and the
   * entry is evicted automatically if the request rejects.
   *
   * @param userId - The identifier of the user to retrieve.
   * @returns A promise resolving to the requested {@link User}. Repeated calls
   * for the same id return the cached promise while it is present.
   */
  getUser(userId: string): Promise<User> {
    const cached = this.cache.get(userId);
    if (cached) {
      return cached;
    }
    const promise = this.userService.getUserAsync({ userId });
    this.cache.set(userId, promise);
    promise.catch(() => this.cache.delete(userId));
    return promise;
  }

  /**
   * Removes cached entries so subsequent {@link getUser} calls fetch fresh data.
   *
   * @param userId - When provided, invalidates only the cache entry for this
   * user id. When omitted, clears the entire cache.
   */
  invalidate(userId?: string) {
    if (userId) {
      this.cache.delete(userId);
    } else {
      this.cache.clear();
    }
  }
}
