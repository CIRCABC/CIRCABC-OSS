import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  FavouritesService,
  Node as ModelNode,
  NodesService,
  PagedNodes,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UserFavouritesComponent } from './user-favourites.component';

describe('UserFavouritesComponent', () => {
  const mockPagedNodes: PagedNodes = {
    data: [
      { id: '1', type: 'content', name: 'file1.pdf' },
      { id: '2', type: 'folder', name: 'myFolder' },
    ],
    total: 2,
  };

  const mockFavouritesService = {
    getFavouritesAsync: vi.fn().mockResolvedValue(mockPagedNodes),
  };
  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('testuser'),
  };
  const mockNodesService = {
    getGroupAsync: vi.fn().mockResolvedValue({ id: 'group1' }),
  };
  const mockRouter = {
    navigate: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockFavouritesService.getFavouritesAsync.mockResolvedValue(mockPagedNodes);

    TestBed.configureTestingModule({
      imports: [UserFavouritesComponent],
      providers: [
        { provide: FavouritesService, useValue: mockFavouritesService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: Router, useValue: mockRouter },
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

  let fixture: ComponentFixture<UserFavouritesComponent>;

  function createComponent(): UserFavouritesComponent {
    fixture = TestBed.createComponent(UserFavouritesComponent);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  it('should load favourites on init', async () => {
    const component = createComponent();
    await fixture.whenStable();

    expect(mockFavouritesService.getFavouritesAsync).toHaveBeenCalledWith({
      userId: 'testuser',
      limit: 10,
      page: 1,
    });
    expect(component.pagedFavourites()).toEqual(mockPagedNodes);
    expect(component.totalItems()).toBe(2);
    expect(component.loading()).toBe(false);
  });

  it('should fall back to empty favourites on error', async () => {
    mockFavouritesService.getFavouritesAsync.mockRejectedValue(
      new Error('fail')
    );
    const component = createComponent();
    await fixture.whenStable();

    expect(component.pagedFavourites()).toEqual({ data: [], total: 0 });
    expect(component.loading()).toBe(false);
  });

  it('should identify a file node', () => {
    const component = createComponent();
    const file: ModelNode = { type: 'content' };
    const folder: ModelNode = { type: 'folder' };
    const noType: ModelNode = {};

    expect(component.isFile(file)).toBe(true);
    expect(component.isFile(folder)).toBe(false);
    expect(component.isFile(noType)).toBe(false);
  });

  it('should identify a folder node', () => {
    const component = createComponent();
    const file: ModelNode = { type: 'content' };
    const folder: ModelNode = { type: 'folder' };
    const noType: ModelNode = {};

    expect(component.isFolder(folder)).toBe(true);
    expect(component.isFolder(file)).toBe(false);
    expect(component.isFolder(noType)).toBe(false);
  });

  it('should navigate to library folder for folder node', async () => {
    const component = createComponent();
    const node: ModelNode = { id: '2', type: 'folder' };

    await component.openLink(node);

    expect(mockNodesService.getGroupAsync).toHaveBeenCalledWith({ id: '2' });
    expect(mockRouter.navigate).toHaveBeenCalledWith([
      '/group',
      'group1',
      'library',
      '2',
    ]);
  });

  it('should navigate to library details for file node', async () => {
    const component = createComponent();
    const node: ModelNode = { id: '1', type: 'content' };

    await component.openLink(node);

    expect(mockNodesService.getGroupAsync).toHaveBeenCalledWith({ id: '1' });
    expect(mockRouter.navigate).toHaveBeenCalledWith([
      '/group',
      'group1',
      'library',
      '1',
      'details',
    ]);
  });

  it('should not navigate when node has no id', async () => {
    const component = createComponent();
    await component.openLink({});

    expect(mockNodesService.getGroupAsync).not.toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should change page and reload favourites', async () => {
    const component = createComponent();
    await fixture.whenStable();

    component.changePage(3);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.page()).toBe(3);
    expect(mockFavouritesService.getFavouritesAsync).toHaveBeenCalledWith({
      userId: 'testuser',
      limit: 10,
      page: 3,
    });
  });

  it('should return true for isPagerVisible when totalItems exceeds limit', async () => {
    mockFavouritesService.getFavouritesAsync.mockResolvedValue({
      data: [],
      total: 15,
    });
    const component = createComponent();
    await fixture.whenStable();

    expect(component.isPagerVisible()).toBe(true);
  });

  it('should return false for isPagerVisible when totalItems is within limit', async () => {
    mockFavouritesService.getFavouritesAsync.mockResolvedValue({
      data: [],
      total: 5,
    });
    const component = createComponent();
    await fixture.whenStable();

    expect(component.isPagerVisible()).toBe(false);
  });
});
