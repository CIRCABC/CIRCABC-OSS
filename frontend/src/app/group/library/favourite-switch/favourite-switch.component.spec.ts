import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  FavouritesService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FavouriteSwitchComponent } from './favourite-switch.component';

describe('FavouriteSwitchComponent', () => {
  let fixture: ComponentFixture<FavouriteSwitchComponent>;
  let component: FavouriteSwitchComponent;
  let componentRef: ComponentRef<FavouriteSwitchComponent>;
  let mockLoginService: {
    isGuest: ReturnType<typeof vi.fn>;
    getCurrentUsername: ReturnType<typeof vi.fn>;
  };
  let mockFavouritesService: {
    deleteFavourite: ReturnType<typeof vi.fn>;
    postFavourite: ReturnType<typeof vi.fn>;
    deleteFavouriteAsync: ReturnType<typeof vi.fn>;
    postFavouriteAsync: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockLoginService = {
      isGuest: vi.fn().mockReturnValue(false),
      getCurrentUsername: vi.fn().mockReturnValue('testuser'),
    };
    mockFavouritesService = {
      deleteFavourite: vi.fn().mockReturnValue(of(undefined)),
      postFavourite: vi.fn().mockReturnValue(of(undefined)),
      deleteFavouriteAsync: vi.fn().mockResolvedValue(undefined),
      postFavouriteAsync: vi.fn().mockResolvedValue(undefined),
    };

    TestBed.configureTestingModule({
      imports: [FavouriteSwitchComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: FavouritesService, useValue: mockFavouritesService },
      ],
    });

    fixture = TestBed.createComponent(FavouriteSwitchComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  function setNode(node: ModelNode): void {
    componentRef.setInput('node', node);
    fixture.detectChanges();
  }

  describe('ngOnInit', () => {
    it('should set shaw to true for a regular file type', () => {
      setNode({ id: '1', type: 'content' });
      expect(component.shaw).toBe(true);
    });

    it('should set shaw to false for filelink type', () => {
      setNode({ id: '1', type: 'filelink' });
      expect(component.shaw).toBe(false);
    });

    it('should set shaw to false for folderlink type', () => {
      setNode({ id: '1', type: 'folderlink' });
      expect(component.shaw).toBe(false);
    });

    it('should set shaw to false when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      setNode({ id: '1', type: 'content' });
      expect(component.shaw).toBe(false);
    });

    it('should set shaw to false when node has no type', () => {
      setNode({ id: '1' });
      expect(component.shaw).toBe(false);
    });
  });

  describe('isFavourite', () => {
    it('should return true when node is favourite', () => {
      setNode({ id: '1', type: 'content', favourite: true });
      expect(component.isFavourite()).toBe(true);
    });

    it('should return false when node is not favourite', () => {
      setNode({ id: '1', type: 'content', favourite: false });
      expect(component.isFavourite()).toBe(false);
    });
  });

  describe('toggleFav', () => {
    it('should call deleteFavourite when node is already favourite', async () => {
      setNode({ id: 'node-1', type: 'content', favourite: true });

      await component.toggleFav();

      expect(mockFavouritesService.deleteFavouriteAsync).toHaveBeenCalledWith({
        userId: 'testuser',
        nodeId: 'node-1',
      });
      expect(component.isFavourite()).toBe(false);
    });

    it('should call postFavourite when node is not favourite', async () => {
      setNode({ id: 'node-1', type: 'content', favourite: false });

      await component.toggleFav();

      expect(mockFavouritesService.postFavouriteAsync).toHaveBeenCalledWith({
        userId: 'testuser',
        simpleId: { id: 'node-1' },
      });
      expect(component.isFavourite()).toBe(true);
    });

    it('should not call service if already working', async () => {
      setNode({ id: 'node-1', type: 'content', favourite: false });

      const promise1 = component.toggleFav();
      const promise2 = component.toggleFav();
      await Promise.all([promise1, promise2]);

      expect(mockFavouritesService.postFavouriteAsync).toHaveBeenCalledTimes(1);
    });
  });
});
