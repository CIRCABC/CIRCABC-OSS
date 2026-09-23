import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ModerateComponent } from './moderate.component';

describe('ModerateComponent', () => {
  let component: ModerateComponent;
  let componentRef: ComponentRef<ModerateComponent>;
  let fixture: ComponentFixture<ModerateComponent>;

  const mockForumService = {
    putModeration: vi.fn().mockReturnValue(of(null)),
    putModerationAsync: vi.fn().mockResolvedValue(null),
  };

  const createForum = (ismoderated = 'false'): ModelNode => ({
    id: 'forum-1',
    properties: { ismoderated },
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModerateComponent],
      providers: [
        { provide: ForumService, useValue: mockForumService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: {
            getTranslation: vi.fn().mockReturnValue(of({})),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ModerateComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('forum', createForum());
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should set moderationEnabled to true when forum is moderated', () => {
      componentRef.setInput('forum', createForum('true'));
      fixture.detectChanges();
      expect(component.moderationEnabled).toBe(true);
    });

    it('should set moderationEnabled to false when forum is not moderated', () => {
      componentRef.setInput('forum', createForum('false'));
      fixture.detectChanges();
      expect(component.moderationEnabled).toBe(false);
    });
  });

  describe('forumModerated', () => {
    it('should return false when properties is undefined', () => {
      componentRef.setInput('forum', { id: 'forum-1' } as ModelNode);
      fixture.detectChanges();
      expect(component.forumModerated()).toBe(false);
    });
  });

  describe('toggleModeration', () => {
    it('should toggle moderationEnabled', () => {
      component.moderationEnabled = false;
      component.toggleModeration();
      expect(component.moderationEnabled).toBe(true);
      component.toggleModeration();
      expect(component.moderationEnabled).toBe(false);
    });
  });

  describe('togglePostValidation', () => {
    it('should toggle acceptPostValidation', () => {
      component.acceptPostValidation = false;
      component.togglePostValidation();
      expect(component.acceptPostValidation).toBe(true);
      component.togglePostValidation();
      expect(component.acceptPostValidation).toBe(false);
    });
  });

  describe('accept', () => {
    it('should call putModeration and emit success result', async () => {
      componentRef.setInput('forum', createForum('false'));
      fixture.detectChanges();
      component.moderationEnabled = true;
      component.acceptPostValidation = true;

      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.accept();

      expect(mockForumService.putModerationAsync).toHaveBeenCalledWith({
        id: 'forum-1',
        enable: true,
        acceptAll: true,
      });
      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.MODERATE_FORUM,
      });
      expect(component.executing()).toBe(false);
    });

    it('should update forum properties after moderation change', async () => {
      const forum = createForum('false');
      componentRef.setInput('forum', forum);
      fixture.detectChanges();
      component.moderationEnabled = true;

      await component.accept();

      expect(forum.properties?.ismoderated).toBe('true');
    });
  });

  describe('cancel', () => {
    it('should hide modal and emit canceled result', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancel('cancel');

      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.MODERATE_FORUM,
      });
    });
  });
});
