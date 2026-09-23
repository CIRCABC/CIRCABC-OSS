import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  LibraryPreferences,
  PreferenceConfiguration,
  SearchConfig,
  SearchPreferences,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UserPreferencesService } from 'app/core/user-preferences.service';
import { of } from 'rxjs';
import { vi } from 'vitest';

const mockLibraryPreferences: LibraryPreferences = {
  column: { name: true },
  listing: { page: 1, limit: 10, sort: 'name' },
};

const mockPreferenceConfiguration: PreferenceConfiguration = {
  library: mockLibraryPreferences,
  search: [
    {
      igId: 'ig123',
      searchConfig: [{ searchName: 'test-search', searchFor: 'hello' }],
    },
  ],
};

describe('UserPreferencesService', () => {
  let service: UserPreferencesService;
  let mockUserService: {
    getUserPreferencesAsync: ReturnType<typeof vi.fn>;
    saveUserPreferences: ReturnType<typeof vi.fn>;
    saveUserPreferencesAsync: ReturnType<typeof vi.fn>;
  };
  let mockLoginService: { getUser: ReturnType<typeof vi.fn> };
  let mockRoute: {
    snapshot: { firstChild: { children: [{ params: { id: string } }] } };
  };

  beforeEach(() => {
    mockUserService = {
      getUserPreferencesAsync: vi
        .fn()
        .mockImplementation(() =>
          Promise.resolve(structuredClone(mockPreferenceConfiguration))
        ),
      saveUserPreferences: vi.fn().mockReturnValue(of(undefined)),
      saveUserPreferencesAsync: vi.fn().mockResolvedValue(undefined),
    };

    mockLoginService = {
      getUser: vi.fn().mockReturnValue({ userId: 'user1' }),
    };

    mockRoute = {
      snapshot: { firstChild: { children: [{ params: { id: 'ig123' } }] } },
    };

    TestBed.configureTestingModule({
      providers: [
        UserPreferencesService,
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: ActivatedRoute, useValue: mockRoute },
      ],
    });

    service = TestBed.inject(UserPreferencesService);
  });

  afterEach(() => {
    localStorage.removeItem('searchPreferences');
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  describe('waitForPreferences', () => {
    it('should load preferences for a logged-in user', async () => {
      const result = await service.waitForPreferences();
      expect(result).toEqual(mockPreferenceConfiguration);
      expect(mockUserService.getUserPreferencesAsync).toHaveBeenCalledWith({
        userId: 'user1',
      });
      expect(service.libraryPreferences).toEqual(mockLibraryPreferences);
    });

    it('should return undefined when user has no userId', async () => {
      mockLoginService.getUser.mockReturnValue({});
      // The service was already constructed with the previous mock, so create a fresh one
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          UserPreferencesService,
          { provide: UserService, useValue: mockUserService },
          {
            provide: LoginService,
            useValue: { getUser: vi.fn().mockReturnValue({}) },
          },
          { provide: ActivatedRoute, useValue: mockRoute },
        ],
      });
      const freshService = TestBed.inject(UserPreferencesService);
      const result = await freshService.waitForPreferences();
      expect(result).toBeUndefined();
    });

    it('should load searchPreferences from localStorage if present', async () => {
      const stored = [
        { igId: 'ig999', searchConfig: [{ searchName: 'saved' }] },
      ];
      localStorage.setItem('searchPreferences', JSON.stringify(stored));

      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          UserPreferencesService,
          { provide: UserService, useValue: mockUserService },
          { provide: LoginService, useValue: mockLoginService },
          { provide: ActivatedRoute, useValue: mockRoute },
        ],
      });
      const freshService = TestBed.inject(UserPreferencesService);
      await freshService.waitForPreferences();
      expect(freshService.searchPreferences).toEqual(stored);
    });
  });

  describe('saveLibraryPreferences', () => {
    it('should update library preferences and save', async () => {
      await service.waitForPreferences();
      const newLib: LibraryPreferences = {
        column: { name: false },
        listing: { page: 2, limit: 25, sort: 'date' },
      };
      service.saveLibraryPreferences(newLib);
      expect(mockUserService.saveUserPreferencesAsync).toHaveBeenCalled();
    });
  });

  describe('getSearchConfiguration', () => {
    it('should return search config for the current IG', async () => {
      await service.waitForPreferences();
      const result = service.getSearchConfiguration();
      expect(result).toEqual([
        { searchName: 'test-search', searchFor: 'hello' },
      ]);
    });

    it('should return empty array when no matching IG', async () => {
      mockRoute.snapshot.firstChild.children[0].params.id = 'unknown';
      await service.waitForPreferences();
      const result = service.getSearchConfiguration();
      expect(result).toEqual([]);
    });
  });

  describe('saveSearchPreferences', () => {
    it('should update existing search config entry', async () => {
      await service.waitForPreferences();
      const updatedConfig: SearchConfig = {
        searchName: 'test-search',
        searchFor: 'world',
      };
      service.saveSearchPreferences(updatedConfig);
      expect(mockUserService.saveUserPreferencesAsync).toHaveBeenCalled();
      const storedRaw = localStorage.getItem('searchPreferences');
      expect(storedRaw).not.toBeNull();
      const stored = JSON.parse(storedRaw as string) as SearchPreferences[];
      expect(stored[0].searchConfig[0].searchFor).toBe('world');
    });

    it('should add new search config to existing IG', async () => {
      await service.waitForPreferences();
      const newConfig: SearchConfig = {
        searchName: 'new-search',
        searchFor: 'foo',
      };
      service.saveSearchPreferences(newConfig);
      expect(mockUserService.saveUserPreferencesAsync).toHaveBeenCalled();
    });

    it('should create new search preferences when IG not found', async () => {
      mockRoute.snapshot.firstChild.children[0].params.id = 'newIg';
      await service.waitForPreferences();
      const config: SearchConfig = {
        searchName: 'brand-new',
        searchFor: 'bar',
      };
      service.saveSearchPreferences(config);
      const storedRaw = localStorage.getItem('searchPreferences');
      expect(storedRaw).not.toBeNull();
      const stored = JSON.parse(storedRaw as string) as SearchPreferences[];
      expect(stored).toEqual([
        {
          igId: 'newIg',
          searchConfig: [{ searchName: 'brand-new', searchFor: 'bar' }],
        },
      ]);
    });
  });

  describe('deleteConfiguration', () => {
    it('should delete a search config by name and save', async () => {
      await service.waitForPreferences();
      mockUserService.saveUserPreferencesAsync.mockClear();
      service.deleteConfiguration('test-search');
      expect(mockUserService.saveUserPreferencesAsync).toHaveBeenCalled();
    });

    it('should not save when search name not found', async () => {
      await service.waitForPreferences();
      mockUserService.saveUserPreferencesAsync.mockClear();
      service.deleteConfiguration('nonexistent');
      expect(mockUserService.saveUserPreferencesAsync).not.toHaveBeenCalled();
    });
  });
});
