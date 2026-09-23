import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  inject,
  input,
  output,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { CreateForumComponent } from 'app/group/forum/create-forum/create-forum.component';
import { CreateTopicComponent } from 'app/group/forum/topic/create-topic.component';

/**
 * Dropdown menu component for forum-related creation actions.
 *
 * Renders a "dropdown-trigger" control that toggles a small menu offering the
 * available actions for the current forum node: creating a new forum and/or
 * creating a new topic. Selecting an action lazily launches the corresponding
 * wizard component ({@link CreateForumComponent} or {@link CreateTopicComponent}),
 * and the result of that wizard is propagated back to the parent through the
 * {@link ForumDropdownComponent.actionFinished} output.
 *
 * The component also listens for clicks outside of its own element in order to
 * automatically close the dropdown and notify the parent via
 * {@link ForumDropdownComponent.clickOutside}.
 */
@Component({
  selector: 'cbc-forum-dropdown',
  templateUrl: './forum-dropdown.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CreateForumComponent, CreateTopicComponent, TranslocoModule],
})
export class ForumDropdownComponent {
  /**
   * Input controlling whether the "add topic" action is offered in the dropdown.
   * Defaults to `true`.
   */
  public readonly enableAddTopic = input(true);
  /**
   * Input controlling whether the "add forum" action is offered in the dropdown.
   * Defaults to `true`.
   */
  public readonly enableAddForum = input(true);

  /**
   * Required input holding the current forum node against which the creation
   * actions (new forum / new topic) are performed.
   */
  public currentNode = input.required<ModelNode>();
  /**
   * Output emitted when a launched wizard finishes, carrying the outcome of the
   * create-forum or create-topic operation.
   */
  public readonly actionFinished = output<ActionEmitterResult>();
  /**
   * Output emitted when a click is detected outside of this component, allowing
   * the parent to react (e.g. dismiss the dropdown). Carries the originating
   * mouse event.
   */
  public readonly clickOutside = output<MouseEvent>();

  /** Whether the actions dropdown menu is currently visible. */
  public showAddDropdown = false;
  /** Whether the create-forum wizard is currently launched/rendered. */
  public launchCreateForum = false;
  /** Whether the create-topic wizard is currently launched/rendered. */
  public launchAddTopic = false;
  /** Reference to this component's host element, used for outside-click detection. */
  private readonly elementRef: ElementRef;

  /**
   * Creates the component and captures the host {@link ElementRef} used to
   * determine whether document clicks occur inside or outside the component.
   */
  public constructor() {
    const myElement = inject(ElementRef);

    this.elementRef = myElement;
  }

  /**
   * Toggles the visibility of the actions dropdown, but only when the click
   * originates from the dropdown trigger element itself (an element carrying the
   * `dropdown-trigger` CSS class). Clicks on other elements are ignored.
   *
   * @param event The DOM event whose target is inspected for the
   *   `dropdown-trigger` class. Typed as `any` to allow direct access to the
   *   event target's class list.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (event.target.classList.contains('dropdown-trigger')) {
      this.showAddDropdown = !this.showAddDropdown;
    }
  }

  /**
   * Document-level click handler that closes the dropdown when a click happens
   * outside this component's host element. When the click is outside, it emits
   * {@link clickOutside} and hides the dropdown.
   *
   * @param event The originating mouse event, re-emitted through {@link clickOutside}.
   * @param targetElement The clicked element; when `null` the handler is a no-op.
   */
  @HostListener('document:click', ['$event', '$event.target'])
  public onClick(event: MouseEvent, targetElement: EventTarget | null): void {
    if (!targetElement) {
      return;
    }

    const clickedInside = this.elementRef.nativeElement.contains(targetElement);
    if (!clickedInside) {
      this.clickOutside.emit(event);
      this.showAddDropdown = false;
    }
  }

  /**
   * Closes the dropdown and toggles the launch state of the create-forum wizard.
   */
  public launchCreateForumWizard(): void {
    this.showAddDropdown = false;
    this.launchCreateForum = !this.launchCreateForum;
  }

  /**
   * Closes the dropdown and toggles the launch state of the create-topic wizard.
   */
  public launchCreateTopicWizard(): void {
    this.showAddDropdown = false;
    this.launchAddTopic = !this.launchAddTopic;
  }

  /**
   * Handles closure of the create-forum wizard, hiding it and forwarding the
   * operation result to the parent via {@link actionFinished}.
   *
   * @param result The outcome of the create-forum operation to propagate.
   */
  public propagateCreateForumClosure(result: ActionEmitterResult): void {
    this.launchCreateForum = false;
    this.actionFinished.emit(result);
  }

  /**
   * Handles closure of the create-topic wizard, hiding it and forwarding the
   * operation result to the parent via {@link actionFinished}.
   *
   * @param result The outcome of the create-topic operation to propagate.
   */
  public propagateCreateTopic(result: ActionEmitterResult): void {
    this.launchAddTopic = false;
    this.actionFinished.emit(result);
  }

  /**
   * Determines whether exactly one of the two creation actions (add forum /
   * add topic) is enabled, which lets the template adapt its layout for a
   * single-action dropdown.
   *
   * @returns `true` if precisely one of add-forum or add-topic is enabled;
   *   `false` when both or neither are enabled.
   */
  public isOnlyOneAction(): boolean {
    const enableAddForum = this.enableAddForum();
    const enableAddTopic = this.enableAddTopic();
    return (
      (enableAddForum && !enableAddTopic) || (!enableAddForum && enableAddTopic)
    );
  }
}
