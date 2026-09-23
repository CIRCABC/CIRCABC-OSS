import { inject, Service } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  LibraryPreferences,
  PreferenceConfiguration,
  SearchConfig,
  SearchPreferences,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';

/**
 * Application-wide singleton service (`providedIn: 'root'`) that loads,
 * caches and persists the current user's preference configuration.
 *
 * Responsibilities:
 * - On construction, eagerly fetches the logged-in user's
 *   {@link PreferenceConfiguration} from the backend and exposes it through a
 *   promise that consumers can await via {@link waitForPreferences}.
 * - Holds the resolved library and search preferences in memory for quick
 *   access by other components/services.
 * - Persists changes back to the backend and mirrors search preferences in
 *   `localStorage`.
 *
 * Key collaborators:
 * - {@link UserService} — generated API client used to read/write user
 *   preferences.
 * - {@link LoginService} — provides the currently authenticated user.
 * - {@link ActivatedRoute} — used to resolve the current interest-group id
 *   (`igId`) from the route so search configurations are scoped per group.
 */
@Service()
export class UserPreferencesService {
  /** Generated API client used to fetch and persist user preferences. */
  private readonly userService = inject(UserService);
  /** Provides access to the currently authenticated user. */
  private readonly loginService = inject(LoginService);
  /** Used to derive the current interest-group id (`igId`) from the route. */
  private readonly route = inject(ActivatedRoute);

  /** The currently authenticated user, lazily resolved from the login service. */
  private user!: User;
  /** The full preference configuration loaded from the backend for the user. */
  private preferenceConfiguration!: PreferenceConfiguration;
  /** Library-specific preferences extracted from {@link preferenceConfiguration}. */
  public libraryPreferences!: LibraryPreferences;
  /** Saved search preferences, hydrated from `localStorage` when available. */
  public searchPreferences!: SearchPreferences[];

  /**
   * Promise created during construction that resolves once the initial
   * preference load completes. Awaited by {@link waitForPreferences}.
   */
  private readonly preferencesPromise: Promise<
    PreferenceConfiguration | undefined
  >;

  /**
   * Kicks off the initial, eager load of the user's preferences and stores the
   * resulting promise so callers can await completion.
   */
  constructor() {
    this.preferencesPromise = this.getPreferences(); // NOSONAR
  }

  /**
   * Returns the promise tracking the initial preference load, allowing callers
   * to await the cached configuration instead of triggering a new fetch.
   *
   * @returns A promise resolving to the loaded {@link PreferenceConfiguration},
   * or `undefined` if no authenticated user was available.
   */
  public async waitForPreferences(): Promise<
    PreferenceConfiguration | undefined
  > {
    return this.preferencesPromise;
  }

  /**
   * Loads the current user's preference configuration from the backend.
   *
   * Resolves the authenticated user (once), and if a user id is present fetches
   * the {@link PreferenceConfiguration}, caches the library preferences and
   * hydrates {@link searchPreferences} from `localStorage` when present.
   *
   * @returns The loaded {@link PreferenceConfiguration}, or `undefined` if there
   * is no authenticated user.
   */
  private async getPreferences(): Promise<PreferenceConfiguration | undefined> {
    this.user ??= this.loginService.getUser();

    if (this.user?.userId) {
      this.preferenceConfiguration =
        await this.userService.getUserPreferencesAsync({
          userId: this.user?.userId,
        });

      this.libraryPreferences = this.preferenceConfiguration.library;
      const seachLocalStore = localStorage.getItem('searchPreferences');
      if (seachLocalStore) {
        this.searchPreferences = JSON.parse(
          seachLocalStore
        ) as SearchPreferences[];
      }
      return this.preferenceConfiguration;
    }
    return undefined;
  }

  /**
   * Persists the in-memory {@link preferenceConfiguration} back to the backend
   * for the current user. Does nothing if no authenticated user id is present.
   */
  public async saveUserPreferences() {
    if (this.user?.userId) {
      await this.userService.saveUserPreferencesAsync({
        userId: this.user.userId,
        preferenceConfiguration: this.preferenceConfiguration,
      });
    }
  }

  /**
   * Updates the library preferences within the cached configuration and
   * persists the whole configuration to the backend.
   *
   * @param libraryPreferences The new library preferences to store.
   */
  public saveLibraryPreferences(libraryPreferences: LibraryPreferences) {
    this.preferenceConfiguration.library = libraryPreferences;
    this.saveUserPreferences();
  }

  /**
   * Returns the saved search configurations scoped to the interest group
   * currently identified by the route (`igId`).
   *
   * @returns The array of {@link SearchConfig} entries for the current group,
   * or an empty array if none are stored or no group is in scope.
   */
  public getSearchConfiguration(): SearchConfig[] {
    if (this.preferenceConfiguration?.search) {
      const searchConfig = this.preferenceConfiguration.search.find(
        (item) =>
          item.igId === this.route.snapshot.firstChild?.children[0].params.id
      );

      if (searchConfig) {
        return searchConfig?.searchConfig;
      }
    }
    return [];
  }

  /**
   * Adds or updates a search configuration for the interest group currently in
   * scope (resolved from the route `igId`).
   *
   * If a configuration with the same `searchName` already exists for the group
   * it is replaced; otherwise the configuration is appended. When the group has
   * no existing preferences, a new {@link SearchPreferences} entry is created.
   * The updated search preferences are mirrored to `localStorage` and the whole
   * configuration is persisted to the backend.
   *
   * @param searchConfig The search configuration to store.
   */
  public saveSearchPreferences(searchConfig: SearchConfig) {
    if (this.route.snapshot.firstChild?.children[0].params.id) {
      const igId: string =
        this.route.snapshot.firstChild?.children[0].params.id;

      let searchPreferenceFound = null;
      if (this.preferenceConfiguration?.search) {
        searchPreferenceFound = this.preferenceConfiguration.search.find(
          (item) => item.igId === igId
        );
      }

      if (searchPreferenceFound) {
        const searchPreferenceFoundIndex =
          this.preferenceConfiguration.search.indexOf(searchPreferenceFound);

        const searchConfigFound = this.preferenceConfiguration.search[
          searchPreferenceFoundIndex
        ].searchConfig.find(
          (item) => item.searchName === searchConfig.searchName
        );

        if (searchConfigFound) {
          const searchConfigFoundIndex =
            this.preferenceConfiguration.search[
              searchPreferenceFoundIndex
            ].searchConfig.indexOf(searchConfigFound);

          this.preferenceConfiguration.search[
            searchPreferenceFoundIndex
          ].searchConfig[searchConfigFoundIndex] = searchConfig;
        } else {
          this.preferenceConfiguration.search[
            searchPreferenceFoundIndex
          ].searchConfig.push(searchConfig);
        }
      } else {
        const searchConf: SearchConfig[] = [];
        searchConf.push(searchConfig);
        const newSearchPreferences: SearchPreferences = {
          igId: igId,
          searchConfig: searchConf,
        };

        this.preferenceConfiguration.search = [newSearchPreferences];
      }
    }
    localStorage.setItem(
      'searchPreferences',
      JSON.stringify(this.preferenceConfiguration.search)
    );
    this.saveUserPreferences();
  }

  /**
   * Removes a stored search configuration by name for the interest group
   * currently in scope (resolved from the route `igId`) and persists the
   * updated configuration to the backend.
   *
   * @param searchName The `searchName` of the configuration to delete.
   */
  public deleteConfiguration(searchName: string) {
    if (this.route.snapshot.firstChild?.children[0].params.id) {
      const igId: string =
        this.route.snapshot.firstChild?.children[0].params.id;

      const searchPreferenceFound = this.preferenceConfiguration.search.find(
        (item) => item.igId === igId
      );

      if (searchPreferenceFound) {
        const searchPreferenceFoundIndex =
          this.preferenceConfiguration.search.indexOf(searchPreferenceFound);

        const searchConfigFound = this.preferenceConfiguration.search[
          searchPreferenceFoundIndex
        ].searchConfig.find((item) => item.searchName === searchName);

        if (searchConfigFound) {
          const searchConfigFoundIndex =
            this.preferenceConfiguration.search[
              searchPreferenceFoundIndex
            ].searchConfig.indexOf(searchConfigFound);
          this.preferenceConfiguration.search[
            searchPreferenceFoundIndex
          ].searchConfig.splice(searchConfigFoundIndex);

          this.saveUserPreferences();
        }
      }
    }
  }
}
