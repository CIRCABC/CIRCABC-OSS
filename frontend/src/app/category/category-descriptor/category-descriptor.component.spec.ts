import { ComponentRef, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { Node as ModelNode, NodesService } from 'app/core/generated/circabc';
import { UrlHelperService } from 'app/core/url-helper.service';
import { SERVER_URL } from 'app/core/variables';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { DownloadUtilService } from 'app/shared/services/download-util.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { CategoryDescriptorComponent } from './category-descriptor.component';

const mockNode: ModelNode = {
  id: 'cat-123',
  name: 'Test Category',
  title: { en: 'English Title' },
  properties: { logoRef: 'workspace://SpacesStore/logo-id' },
};

describe('CategoryDescriptorComponent', () => {
  let component: CategoryDescriptorComponent;
  let componentRef: ComponentRef<CategoryDescriptorComponent>;
  let fixture: ComponentFixture<CategoryDescriptorComponent>;
  let actionFinishedSubject: Subject<ActionEmitterResult>;

  // The component consumes the signal-based `getNodeResource` factory from the
  // generated circabc client. We fake the returned HttpResourceRef with a writable
  // value signal and a reload spy so the component contract can be asserted
  // without exercising the real HTTP stack.
  const nodeValue = signal<ModelNode | undefined>(mockNode);
  const reloadSpy = vi.fn();
  const mockNodesService = {
    getNodeResource: vi.fn(() => ({ value: nodeValue, reload: reloadSpy })),
  };

  const mockI18nPipe = {
    transform: vi.fn((mltext: { [key: string]: string } | undefined) => {
      if (!mltext) return '';
      return mltext['en'] ?? '';
    }),
  };

  beforeEach(async () => {
    actionFinishedSubject = new Subject<ActionEmitterResult>();
    vi.clearAllMocks();
    nodeValue.set(mockNode);

    await TestBed.configureTestingModule({
      imports: [CategoryDescriptorComponent],
      providers: [
        { provide: NodesService, useValue: mockNodesService },
        {
          provide: ActionService,
          useValue: { actionFinished$: actionFinishedSubject.asObservable() },
        },
        { provide: I18nPipe, useValue: mockI18nPipe },
        {
          provide: DownloadUtilService,
          useValue: { getDownloadUrl: vi.fn().mockReturnValue('http://mock') },
        },
        {
          provide: UrlHelperService,
          useValue: { get: vi.fn().mockReturnValue(of('blob:mock')) },
        },
        { provide: SERVER_URL, useValue: 'http://localhost/' },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryDescriptorComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('categoryId', 'cat-123');
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should fetch the category node via getNodeResource', () => {
    expect(mockNodesService.getNodeResource).toHaveBeenCalled();
    expect(component.category()).toEqual(mockNode);
  });

  it('should reload the resource when actionFinished$ emits UPDATE_CATEGORY success', async () => {
    reloadSpy.mockClear();

    actionFinishedSubject.next({
      type: ActionType.UPDATE_CATEGORY,
      result: ActionResult.SUCCEED,
      node: { id: 'cat-123' },
    });
    await fixture.whenStable();

    expect(reloadSpy).toHaveBeenCalledTimes(1);
  });

  it('should reload the resource when actionFinished$ emits ADD_CATEGORY_LOGO success', async () => {
    reloadSpy.mockClear();

    actionFinishedSubject.next({
      type: ActionType.ADD_CATEGORY_LOGO,
      result: ActionResult.SUCCEED,
      node: { id: 'cat-123' },
    });
    await fixture.whenStable();

    expect(reloadSpy).toHaveBeenCalledTimes(1);
  });

  it('should not reload when action result is not SUCCEED', async () => {
    reloadSpy.mockClear();

    actionFinishedSubject.next({
      type: ActionType.UPDATE_CATEGORY,
      result: ActionResult.CANCELED,
      node: { id: 'cat-123' },
    });
    await fixture.whenStable();

    expect(reloadSpy).not.toHaveBeenCalled();
  });

  describe('hasModelNodeLogo', () => {
    it('should return true when logoRef is set', () => {
      expect(component.hasModelNodeLogo()).toBe(true);
    });

    it('should return false when properties is undefined', () => {
      component.category.set({ id: 'cat-123' } as ModelNode);
      expect(component.hasModelNodeLogo()).toBe(false);
    });

    it('should return false when logoRef is empty', () => {
      component.category.set({ properties: { logoRef: '' } });
      expect(component.hasModelNodeLogo()).toBe(false);
    });

    it('should return false when logoRef is undefined', () => {
      component.category.set({ properties: {} });
      expect(component.hasModelNodeLogo()).toBe(false);
    });
  });

  describe('getLogoRef', () => {
    it('should return last segment of logoRef path', () => {
      expect(component.getLogoRef()).toBe('logo-id');
    });

    it('should return empty string when properties is undefined', () => {
      component.category.set({ id: 'cat-123' } as ModelNode);
      expect(component.getLogoRef()).toBe('');
    });
  });

  describe('getCategoryGroupDescription', () => {
    it('should return transformed title', () => {
      expect(component.getCategoryGroupDescription()).toBe('English Title');
    });

    it('should return name when title transforms to empty', () => {
      mockI18nPipe.transform.mockReturnValue('');
      component.category.set({ title: { en: '' }, name: 'Fallback Name' });
      expect(component.getCategoryGroupDescription()).toBe('Fallback Name');
    });

    it('should return empty string when category has no title and no name', () => {
      component.category.set({} as ModelNode);
      expect(component.getCategoryGroupDescription()).toBe('');
    });
  });

  it('should unsubscribe on destroy', () => {
    fixture.destroy();
    // Should not throw after destroy
    expect(() => {
      actionFinishedSubject.next({
        type: ActionType.UPDATE_CATEGORY,
        result: ActionResult.SUCCEED,
        node: { id: 'cat-123' },
      });
    }).not.toThrow();
  });
});
