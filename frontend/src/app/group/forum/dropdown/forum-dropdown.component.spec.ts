import { Component, ComponentRef, input, output } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionEmitterResult, ActionType } from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ForumDropdownComponent } from './forum-dropdown.component';

@Component({ selector: 'cbc-create-forum', template: '' })
class MockCreateForumComponent {
  parentNode = input<ModelNode>();
  showWizard = input(false);
  modalHide = output<ActionEmitterResult>();
}

@Component({ selector: 'cbc-create-topic', template: '' })
class MockCreateTopicComponent {
  forum = input<ModelNode>();
  showWizard = input(false);
  modalHide = output<ActionEmitterResult>();
}

describe('ForumDropdownComponent', () => {
  let component: ForumDropdownComponent;
  let componentRef: ComponentRef<ForumDropdownComponent>;
  let fixture: ComponentFixture<ForumDropdownComponent>;

  const mockNode: ModelNode = { id: '123', name: 'test-node' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ForumDropdownComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(ForumDropdownComponent, {
      set: {
        imports: [
          TranslocoModule,
          MockCreateForumComponent,
          MockCreateTopicComponent,
        ],
      },
    });

    fixture = TestBed.createComponent(ForumDropdownComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('currentNode', mockNode);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('toggleAddDropdown', () => {
    it('should toggle showAddDropdown when target has dropdown-trigger class', () => {
      const event = {
        target: { classList: { contains: () => true } },
      };
      component.toggleAddDropdown(event);
      expect(component.showAddDropdown).toBe(true);

      component.toggleAddDropdown(event);
      expect(component.showAddDropdown).toBe(false);
    });

    it('should not toggle when target does not have dropdown-trigger class', () => {
      const event = {
        target: { classList: { contains: () => false } },
      };
      component.toggleAddDropdown(event);
      expect(component.showAddDropdown).toBe(false);
    });
  });

  describe('onClick', () => {
    it('should hide dropdown and emit clickOutside when clicking outside', () => {
      component.showAddDropdown = true;
      const outsideElement = document.createElement('div');
      document.body.appendChild(outsideElement);

      const clickOutsideSpy = vi.fn();
      component.clickOutside.subscribe(clickOutsideSpy);

      const event = new MouseEvent('click');
      component.onClick(event, outsideElement);

      expect(component.showAddDropdown).toBe(false);
      expect(clickOutsideSpy).toHaveBeenCalledWith(event);

      document.body.removeChild(outsideElement);
    });

    it('should do nothing when targetElement is null', () => {
      component.showAddDropdown = true;
      component.onClick(new MouseEvent('click'), null);
      expect(component.showAddDropdown).toBe(true);
    });
  });

  describe('launchCreateForumWizard', () => {
    it('should hide dropdown and toggle launchCreateForum', () => {
      component.showAddDropdown = true;
      component.launchCreateForumWizard();
      expect(component.showAddDropdown).toBe(false);
      expect(component.launchCreateForum).toBe(true);
    });
  });

  describe('launchCreateTopicWizard', () => {
    it('should hide dropdown and toggle launchAddTopic', () => {
      component.showAddDropdown = true;
      component.launchCreateTopicWizard();
      expect(component.showAddDropdown).toBe(false);
      expect(component.launchAddTopic).toBe(true);
    });
  });

  describe('propagateCreateForumClosure', () => {
    it('should reset launchCreateForum and emit actionFinished', () => {
      component.launchCreateForum = true;
      const result: ActionEmitterResult = { type: ActionType.CREATE_FORUM };

      const actionSpy = vi.fn();
      component.actionFinished.subscribe(actionSpy);

      component.propagateCreateForumClosure(result);

      expect(component.launchCreateForum).toBe(false);
      expect(actionSpy).toHaveBeenCalledWith(result);
    });
  });

  describe('propagateCreateTopic', () => {
    it('should reset launchAddTopic and emit actionFinished', () => {
      component.launchAddTopic = true;
      const result: ActionEmitterResult = { type: ActionType.CREATE_TOPIC };

      const actionSpy = vi.fn();
      component.actionFinished.subscribe(actionSpy);

      component.propagateCreateTopic(result);

      expect(component.launchAddTopic).toBe(false);
      expect(actionSpy).toHaveBeenCalledWith(result);
    });
  });

  describe('isOnlyOneAction', () => {
    it.each([
      [true, false, true, 'only forum is enabled'],
      [false, true, true, 'only topic is enabled'],
      [true, true, false, 'both are enabled'],
      [false, false, false, 'neither is enabled'],
    ])(
      'should return %s when %s',
      (enableAddForum, enableAddTopic, expected) => {
        componentRef.setInput('enableAddForum', enableAddForum);
        componentRef.setInput('enableAddTopic', enableAddTopic);
        fixture.detectChanges();
        expect(component.isOnlyOneAction()).toBe(expected);
      }
    );
  });
});
