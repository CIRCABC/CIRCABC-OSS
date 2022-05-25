import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
} from '@angular/core';

import { ActionEmitterResult } from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-forum-dropdown',
  templateUrl: './forum-dropdown.component.html',
  preserveWhitespaces: true,
})
export class ForumDropdownComponent {
  @Input()
  public enableAddTopic = true;
  @Input()
  public enableAddForum = true;
  @Input()
  public currentNode!: ModelNode;
  @Output()
  public readonly actionFinished = new EventEmitter<ActionEmitterResult>();
  @Output()
  public readonly clickOutside = new EventEmitter<MouseEvent>();

  public showAddDropdown = false;
  public launchCreateForum = false;
  public launchAddTopic = false;
  private elementRef: ElementRef;

  public constructor(myElement: ElementRef) {
    this.elementRef = myElement;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (event.target.classList.contains('dropdown-trigger')) {
      this.showAddDropdown = !this.showAddDropdown;
    }
  }

  @HostListener('document:click', ['$event', '$event.target'])
  public onClick(event: MouseEvent, targetElement: HTMLElement): void {
    if (!targetElement) {
      return;
    }

    const clickedInside = this.elementRef.nativeElement.contains(targetElement);
    if (!clickedInside) {
      this.clickOutside.emit(event);
      this.showAddDropdown = false;
    }
  }

  public launchCreateForumWizard(): void {
    this.showAddDropdown = false;
    this.launchCreateForum = !this.launchCreateForum;
  }

  public launchCreateTopicWizard(): void {
    this.showAddDropdown = false;
    this.launchAddTopic = !this.launchAddTopic;
  }

  public propagateCreateForumClosure(result: ActionEmitterResult): void {
    this.launchCreateForum = false;
    this.actionFinished.emit(result);
  }

  public propagateCreateTopic(result: ActionEmitterResult): void {
    this.launchAddTopic = false;
    this.actionFinished.emit(result);
  }

  public isOnlyOneAction(): boolean {
    return (
      (this.enableAddForum && !this.enableAddTopic) ||
      (!this.enableAddForum && this.enableAddTopic)
    );
  }
}
