import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  User,
  UserService,
  PreferenceConfiguration,
  SearchPreferences,
  LibraryPreferences,
  SearchConfig,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UserPreferencesService {
  private user!: User;
  private preferenceConfiguration!: PreferenceConfiguration;
  public libraryPreferences!: LibraryPreferences;
  public searchPreferences!: SearchPreferences[];

  constructor(
    private userService: UserService,
    private loginService: LoginService,
    private route: ActivatedRoute
  ) {
    this.getPreferences();
  }

  private async getPreferences() {
    if (this.user === undefined || this.user === null) {
      this.user = this.loginService.getUser();
    }

    if (this.user?.userId) {
      this.preferenceConfiguration = await firstValueFrom(
        this.userService.getUserPreferences(this.user?.userId)
      );

      this.libraryPreferences = this.preferenceConfiguration.library;
      const seachLocalStore = localStorage.getItem('searchPreferences');
      if (seachLocalStore) {
        this.searchPreferences = JSON.parse(
          seachLocalStore
        ) as SearchPreferences[];
      }
    }
  }

  public async saveUserPreferences() {
    if (this.user?.userId) {
      await firstValueFrom(
        this.userService.saveUserPreferences(
          this.user.userId,
          this.preferenceConfiguration
        )
      );
    }
  }

  public saveLibraryPreferences(libraryPreferences: LibraryPreferences) {
    this.preferenceConfiguration.library = libraryPreferences;
    this.saveUserPreferences();
  }

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
